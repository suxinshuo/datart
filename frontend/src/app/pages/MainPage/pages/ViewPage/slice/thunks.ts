/**
 * Datart
 *
 * Copyright 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { createAsyncThunk } from '@reduxjs/toolkit';
import sqlReservedWords from 'app/assets/javascripts/sqlReservedWords';
import migrationViewConfig from 'app/migration/ViewConfig/migrationViewConfig';
import { migrateViewConfig } from 'app/migration/ViewConfig/migrationViewDetailConfig';
import beginViewModelMigration from 'app/migration/ViewConfig/migrationViewModelConfig';
import { getCascadeAccess } from 'app/pages/MainPage/Access';
import {
  selectIsOrgOwner,
  selectOrgId,
  selectPermissionMap,
} from 'app/pages/MainPage/slice/selectors';
import i18n from 'i18next';
import { monaco } from 'react-monaco-editor';
import { RootState } from 'types';
import { request2 } from 'utils/request';
import {
  errorHandle,
  getErrorMessage,
  getPath,
  rejectHandle,
} from 'utils/utils';
import { viewActions } from '.';
import { View } from '../../../../../types/View';
import {
  PermissionLevels,
  ResourceTypes,
} from '../../PermissionPage/constants';
import { selectVariables } from '../../VariablePage/slice/selectors';
import { Variable } from '../../VariablePage/slice/types';
import { ViewViewModelStages } from '../constants';
import {
  buildRequestColumns,
  diffMergeHierarchyModel,
  generateEditingView,
  generateNewEditingViewName,
  getSaveParamsFromViewModel,
  getSqlFromCache,
  handleObjectScriptToString,
  isCacheExpired,
  isNewView,
  transformModelToViewModel,
  transformQueryResultToModelAndDataSource,
} from '../utils';
import {
  selectAllSourceDatabaseSchemas,
  selectCurrentEditingView,
  selectCurrentEditingViewAttr,
  selectCurrentEditingViewKey,
  selectEditingViews,
  selectSourceDatabaseSchemas,
  selectViews,
} from './selectors';
import {
  DeleteViewParams,
  QueryResult,
  SaveFolderParams,
  SaveViewParams,
  SqlTaskCancelResponse,
  SqlTaskCreateResponse,
  SqlTaskStatus,
  SqlTaskStatusResponse,
  StructViewQueryProps,
  UnarchiveViewParams,
  UpdateViewBaseParams,
  VariableHierarchy,
  ViewBase,
  ViewSimple,
  ViewViewModel,
} from './types';

export const getViews = createAsyncThunk<ViewSimple[], string>(
  'view/getViews',
  async orgId => {
    const { data } = await request2<ViewSimple[]>(`/views?orgId=${orgId}`);
    return data;
  },
);

export const getArchivedViews = createAsyncThunk<ViewSimple[], string>(
  'view/getArchivedViews',
  async orgId => {
    const { data } = await request2<ViewSimple[]>(
      `/views/archived?orgId=${orgId}`,
    );
    return data;
  },
);

export const getViewDetail = createAsyncThunk<
  ViewViewModel,
  { viewId: string },
  { state: RootState }
>(
  'view/getViewDetail',
  async ({ viewId }, { dispatch, getState, rejectWithValue }) => {
    const views = selectViews(getState());
    const editingViews = selectEditingViews(getState());
    const selected = editingViews.find(v => v.id === viewId);

    if (selected) {
      dispatch(viewActions.switchCurrentEditingView(viewId));
      return selected;
    }

    if (isNewView(viewId)) {
      // Check SQL cache for this view
      const cachedSql = getSqlFromCache(viewId);
      if (cachedSql) {
        // Use cached name if available, otherwise generate new name
        const viewName =
          cachedSql.name || generateNewEditingViewName(editingViews);
        const newView = generateEditingView({
          id: viewId,
          name: viewName,
          type: 'SQL', // Set type to SQL since it's a SQL view cache
          script: cachedSql.script,
          sourceId: cachedSql.sourceId,
        });
        dispatch(viewActions.addEditingView(newView));
        return newView;
      }

      const newView = generateEditingView({
        id: viewId,
        name: generateNewEditingViewName(editingViews),
      });
      dispatch(viewActions.addEditingView(newView));
      return newView;
    }

    const viewSimple = views?.find(v => v.id === viewId);
    const tempViewModel = generateEditingView({
      id: viewId,
      name: viewSimple?.name || i18n.t('view.loading'),
      stage: ViewViewModelStages.Loading,
    });

    dispatch(viewActions.addEditingView(tempViewModel));

    let { data } = await request2<View>(`/views/${viewId}`, undefined, {
      onRejected: error => {
        return rejectHandle(error, rejectWithValue);
      },
    });
    data = migrationViewConfig(data);
    data.config = migrateViewConfig(data.config);
    data.model = beginViewModelMigration(data?.model, data.type);

    // Check browser cache for SQL content if it's a SQL view
    let cacheConflict = false;
    let cacheExpired = false;
    let cacheData:
      | {
          script: string;
          name: string;
          sourceId: string;
          updatedAt: number;
          viewId: string;
        }
      | undefined = undefined;

    if (data.type === 'SQL') {
      const cachedSql = getSqlFromCache(viewId);
      if (cachedSql) {
        // Check if cache content or sourceId is different from backend data (regardless of expiration)
        const isContentDifferent =
          cachedSql.script !== data.script ||
          cachedSql.sourceId !== (data as any).sourceId;

        if (isContentDifferent) {
          // Compare update times to determine which is newer
          const date = new Date(
            (data as any).updateTime.replace(' ', 'T') + '+08:00',
          );
          const backendUpdatedAt = date.getTime();

          // Save original server data for conflict resolution
          const originalServerData = {
            script: data.script,
            sourceId: (data as any).sourceId,
          };

          // Always use local cache for display when there's a conflict
          (data as any).script = cachedSql.script;
          (data as any).sourceId = cachedSql.sourceId;
          (data as any).touched = true;

          if (!(backendUpdatedAt && cachedSql.updatedAt > backendUpdatedAt)) {
            // Only prompt for conflict if backend data is newer
            cacheConflict = true;
            cacheData = cachedSql;
            (data as any).originalServerData = originalServerData;
          } else if (isCacheExpired(cachedSql)) {
            // Only handle expiration if no conflict exists
            cacheExpired = true;
            cacheData = cachedSql;
            // Use cached data for display
            (data as any).script = cachedSql.script;
            (data as any).sourceId = cachedSql.sourceId;
            (data as any).touched = true;
          }
        }
      }
    }

    const viewModel = transformModelToViewModel(data, null, tempViewModel);

    // Add cache-related fields to the view model
    viewModel.cacheConflict = cacheConflict;
    viewModel.cacheExpired = cacheExpired;
    viewModel.cacheData = cacheData;

    return viewModel;
  },
);

export const getSchemaBySourceId = createAsyncThunk<any, string>(
  'source/getSchemaBySourceId',
  async (sourceId, { getState }) => {
    const sourceSchemas = selectSourceDatabaseSchemas(getState() as RootState, {
      id: sourceId,
    });
    if (sourceSchemas) {
      return;
    }
    const { data } = await request2<any>({
      url: `/sources/schemas/${sourceId}`,
      method: 'GET',
    });
    return {
      sourceId,
      data,
    };
  },
);

// Helper function to build request data for SQL execution
const buildSqlExecutionRequest = (
  currentEditingView: ViewViewModel,
  scriptProps: StructViewQueryProps | undefined,
  allDatabaseSchemas: any,
) => {
  const { id, sourceId, size, fragment, variables, type, sparkShareLevel } =
    currentEditingView;
  let sql = '';
  let structure: StructViewQueryProps | null = null;
  let script = '';

  if (scriptProps) {
    structure = scriptProps;
  } else {
    if (type === 'SQL') {
      sql = currentEditingView.script as string;
    } else {
      structure = currentEditingView.script as StructViewQueryProps;
    }
  }

  // Validate required data based on view type
  if (type === 'SQL') {
    if (!sql && !fragment) {
      throw new Error(i18n.t('view.sqlRequired'));
    }
    script = fragment || sql;
  } else {
    if (!structure) {
      throw new Error(i18n.t('view.structQueryRequired'));
    }
    if (
      !currentEditingView.sourceId ||
      !allDatabaseSchemas[currentEditingView.sourceId]
    ) {
      throw new Error(i18n.t('view.sourceNotAvailable'));
    }
    script = handleObjectScriptToString(
      structure,
      allDatabaseSchemas[currentEditingView.sourceId],
    );
  }

  let reqColumns = '';
  if (type === 'STRUCT' && structure) {
    reqColumns = buildRequestColumns(structure);
  }

  // Validate request data
  if (!sourceId) {
    throw new Error(i18n.t('view.selectSource'));
  }
  if (!script.trim()) {
    throw new Error(
      type === 'SQL'
        ? i18n.t('view.sqlRequired')
        : i18n.t('view.structQueryRequired'),
    );
  }

  const requestData = {
    script,
    sourceId,
    size,
    scriptType: type,
    columns: reqColumns,
    variables: variables.map(
      ({ name, type, valueType, defaultValue, expression }) => ({
        name,
        type,
        valueType,
        values: defaultValue ? JSON.parse(defaultValue) : null,
        expression,
      }),
    ),
    // Add viewId if it exists, otherwise it will be undefined
    viewId: id,
    // Add Spark resource isolation level if provided
    sparkShareLevel: sparkShareLevel || 'USER',
  };

  return requestData;
};

// Helper function to handle synchronous SQL execution
export const runSqlSync = async (
  requestData: any,
  reqColumns: string,
  dispatch: any,
) => {
  const response = await request2<QueryResult>(
    {
      url: '/data-provider/execute/test',
      method: 'POST',
      data: requestData,
    },
    undefined,
    {
      onRejected: error => {
        // Error handling will be done in runSql.fulfilled/rejected reducers
        // which already have race condition protection
      },
    },
  );
  return {
    ...response?.data,
    warnings: response?.warnings,
    reqColumns: reqColumns,
  };
};

// Helper function to update task status consistently
export const updateTaskStatus = (
  dispatch: any,
  taskId: string | undefined,
  status: SqlTaskStatus,
  progress: number = 0,
  errorMessage?: string,
) => {
  const statusUpdate = {
    currentTaskId: taskId,
    currentTaskStatus: status,
    currentTaskProgress: progress,
    currentTaskErrorMessage: errorMessage,
  };

  // If task is completed, update progress
  if (status === SqlTaskStatus.SUCCESS || status === SqlTaskStatus.FAILED) {
    statusUpdate.currentTaskProgress =
      status === SqlTaskStatus.SUCCESS ? 100 : progress;
  }

  dispatch(viewActions.changeCurrentEditingView(statusUpdate));
};

export const updateTaskStatusWithoutProgress = (
  dispatch: any,
  taskId: string | undefined,
  status: SqlTaskStatus,
  errorMessage?: string,
) => {
  const statusUpdate = {
    currentTaskId: taskId,
    currentTaskStatus: status,
    currentTaskErrorMessage: errorMessage,
  };
  dispatch(viewActions.changeCurrentEditingView(statusUpdate));
};

// Helper function to handle asynchronous SQL execution
export const runSqlAsync = async (requestData: any, dispatch: any) => {
  try {
    const response = await request2<SqlTaskCreateResponse>({
      url: '/execute/sql',
      method: 'POST',
      data: requestData,
    });

    // Store task information in state
    updateTaskStatus(dispatch, response.data.taskId, SqlTaskStatus.QUEUED, 0);

    return response.data;
  } catch (error) {
    const errorMsg = getErrorMessage(error);
    dispatch(
      viewActions.changeCurrentEditingView({
        stage: ViewViewModelStages.Initialized,
        error: errorMsg,
      }),
    );
    updateTaskStatus(dispatch, undefined, SqlTaskStatus.FAILED, 0, errorMsg);
    return null;
  }
};

export const runSql = createAsyncThunk<
  QueryResult | null | SqlTaskCreateResponse,
  { id: string; isFragment: boolean; script?: StructViewQueryProps },
  { state: RootState }
>('view/runSql', async ({ script: scriptProps }, { getState, dispatch }) => {
  try {
    const currentEditingView = selectCurrentEditingView(
      getState(),
    ) as ViewViewModel;

    // Ensure we have a valid current editing view
    if (!currentEditingView) {
      throw new Error(i18n.t('view.noCurrentEditingView'));
    }

    // Check if view is currently being saved, if so, don't allow running SQL
    if (currentEditingView.stage === ViewViewModelStages.Saving) {
      throw new Error(i18n.t('view.saveInProgress'));
    }

    // Set stage to Running at the beginning of execution
    dispatch(
      viewActions.changeCurrentEditingView({
        stage: ViewViewModelStages.Running,
        error: undefined,
        isCancelClicked: false,
      }),
    );

    const allDatabaseSchemas = selectAllSourceDatabaseSchemas(getState());

    const { enableAsyncExecution } = currentEditingView;

    // Build request data with validation
    const requestData = buildSqlExecutionRequest(
      currentEditingView,
      scriptProps,
      allDatabaseSchemas,
    );

    // Execute based on async flag
    if (enableAsyncExecution) {
      return await runSqlAsync(requestData, dispatch);
    } else {
      return await runSqlSync(requestData, requestData.columns, dispatch);
    }
  } catch (error) {
    const errorMsg = getErrorMessage(error);
    dispatch(
      viewActions.changeCurrentEditingView({
        stage: ViewViewModelStages.Initialized,
        error: errorMsg,
      }),
    );
    updateTaskStatus(dispatch, undefined, SqlTaskStatus.FAILED, 0, errorMsg);
    return {} as any;
  }
});

// New async SQL task functions
export const getSqlTaskStatus = createAsyncThunk<
  SqlTaskStatusResponse,
  { taskId: string },
  { state: RootState }
>('view/getSqlTaskStatus', async ({ taskId }, { getState, dispatch }) => {
  try {
    // Validate taskId parameter
    if (!taskId || typeof taskId !== 'string') {
      const errorMsg = i18n.t('view.invalidTaskId');
      updateTaskStatus(dispatch, undefined, SqlTaskStatus.FAILED, 0, errorMsg);
      throw new Error(errorMsg);
    }

    const response = await request2<SqlTaskStatusResponse>({
      url: `/execute/tasks/${taskId}`,
      method: 'GET',
    });

    // Update task status in state
    const currentEditingView = selectCurrentEditingView(
      getState(),
    ) as ViewViewModel;
    if (currentEditingView && currentEditingView.currentTaskId === taskId) {
      updateTaskStatus(
        dispatch,
        taskId,
        response.data.status,
        response.data.progress,
        response.data.errorMessage || undefined,
      );

      // If task is complete, update the results
      if (
        response.data.status === SqlTaskStatus.SUCCESS &&
        response.data.taskResult
      ) {
        // Transform query result to model and data source, similar to synchronous execution
        const { model, dataSource } = transformQueryResultToModelAndDataSource(
          response.data.taskResult,
          currentEditingView.model,
          currentEditingView.type,
        );

        // Update model and preview results - only if this is still the active task
        // and the view is still in Running stage (to prevent race conditions with new queries)
        // Don't set stage to Saveable - we'll let saveView handle setting to Saved
        if (
          currentEditingView.currentTaskId === taskId &&
          currentEditingView.stage === ViewViewModelStages.Running
        ) {
          dispatch(
            viewActions.changeCurrentEditingView({
              model: diffMergeHierarchyModel(model, currentEditingView.type!),
              previewResults: dataSource,
              warnings: response.data.taskResult.warnings,
              originalRowCount: response.data.originalRowCount,
              displayedRowCount: response.data.displayedRowCount,
              truncated: response.data.truncated,
            }),
          );
        }

        // Auto save the view after successful execution - only if this is still the active task and it's not a new view
        if (
          currentEditingView.currentTaskId === taskId &&
          currentEditingView.stage === ViewViewModelStages.Running &&
          !isNewView(currentEditingView.id) // Only auto save for persisted views
        ) {
          // Check if user has manage permission before auto saving
          const isOwner = selectIsOrgOwner(getState());
          const permissionMap = selectPermissionMap(getState());
          const views = selectViews(getState());

          // Build view path for permission check
          const path = views
            ? getPath(
                views as Array<{ id: string; parentId: string }>,
                {
                  id: currentEditingView.id,
                  parentId: currentEditingView.parentId,
                },
                ResourceTypes.View,
              )
            : [];

          const hasManagePermission = getCascadeAccess(
            isOwner,
            permissionMap,
            ResourceTypes.View,
            path,
            PermissionLevels.Manage,
          );

          // Only auto save if user has manage permission
          if (hasManagePermission) {
            dispatch(saveView({}));
          }
        }
      } else if (response.data.status === SqlTaskStatus.FAILED) {
        dispatch(
          viewActions.changeCurrentEditingView({
            stage: ViewViewModelStages.Initialized,
            error:
              response.data.errorMessage || i18n.t('view.sqlExecutionFailed'),
          }),
        );

        // Auto save the view after failed execution - only if it's not a new view
        if (
          currentEditingView.currentTaskId === taskId &&
          !isNewView(currentEditingView.id) // Only auto save for persisted views
        ) {
          // Check if user has manage permission before auto saving
          const isOwner = selectIsOrgOwner(getState());
          const permissionMap = selectPermissionMap(getState());
          const views = selectViews(getState());

          // Build view path for permission check
          const path = views
            ? getPath(
                views as Array<{ id: string; parentId: string }>,
                {
                  id: currentEditingView.id,
                  parentId: currentEditingView.parentId,
                },
                ResourceTypes.View,
              )
            : [];

          const hasManagePermission = getCascadeAccess(
            isOwner,
            permissionMap,
            ResourceTypes.View,
            path,
            PermissionLevels.Manage,
          );

          // Only auto save if user has manage permission
          if (hasManagePermission) {
            dispatch(saveView({}));
          }
        }
      }
    }

    return response.data;
  } catch (error) {
    const errorMsg = getErrorMessage(error);
    errorHandle(error);

    // Only update status if this is still the current task
    const currentEditingView = selectCurrentEditingView(
      getState(),
    ) as ViewViewModel;
    if (currentEditingView && currentEditingView.currentTaskId === taskId) {
      updateTaskStatusWithoutProgress(
        dispatch,
        taskId,
        SqlTaskStatus.FAILED,
        errorMsg,
      );
      dispatch(
        viewActions.changeCurrentEditingView({
          stage: ViewViewModelStages.Initialized,
          error: errorMsg,
        }),
      );
    }

    throw error;
  }
});

export const cancelSqlTask = createAsyncThunk<
  SqlTaskCancelResponse,
  { taskId: string },
  { state: RootState }
>('view/cancelSqlTask', async ({ taskId }, { getState, dispatch }) => {
  try {
    // Validate taskId parameter
    if (!taskId || typeof taskId !== 'string') {
      const errorMsg = i18n.t('view.invalidTaskId');
      errorHandle(new Error(errorMsg));

      // Only update status if there's an active task
      const currentEditingView = selectCurrentEditingView(
        getState(),
      ) as ViewViewModel;
      if (currentEditingView && currentEditingView.currentTaskId) {
        updateTaskStatusWithoutProgress(
          dispatch,
          currentEditingView.currentTaskId,
          SqlTaskStatus.FAILED,
          errorMsg,
        );
        dispatch(
          viewActions.changeCurrentEditingView({
            stage: ViewViewModelStages.Initialized,
            error: errorMsg,
          }),
        );
      }

      throw new Error(errorMsg);
    }

    // Check if this task is still the current active task
    const currentEditingView = selectCurrentEditingView(
      getState(),
    ) as ViewViewModel;
    if (!currentEditingView || currentEditingView.currentTaskId !== taskId) {
      const errorMsg = i18n.t('view.taskNotActive');
      errorHandle(new Error(errorMsg));
      throw new Error(errorMsg);
    }

    const response = await request2<SqlTaskCancelResponse>({
      url: `/execute/tasks/${taskId}/cancel`,
      method: 'POST',
    });

    // Update task status based on cancel result
    // According to the type definition, cancelResult indicates the cancellation outcome
    const isSuccess = response.data.cancelResult === 'SUCCESS';

    if (isSuccess) {
      // Don't clear taskId yet - continue polling to get final task status
      // Just update the status to show cancellation
      updateTaskStatusWithoutProgress(
        dispatch,
        taskId,
        SqlTaskStatus.FAILED,
        i18n.t('view.sqlExecutionCancelled'),
      );
      dispatch(
        viewActions.changeCurrentEditingView({
          stage: ViewViewModelStages.Initialized,
        }),
      );
    } else {
      const errorMsg = i18n.t('view.cancelTaskFailed');
      updateTaskStatusWithoutProgress(
        dispatch,
        taskId,
        SqlTaskStatus.FAILED,
        errorMsg,
      );
      dispatch(
        viewActions.changeCurrentEditingView({
          stage: ViewViewModelStages.Initialized,
          error: errorMsg,
        }),
      );
    }

    return response.data;
  } catch (error) {
    const errorMsg = getErrorMessage(error);
    errorHandle(error);

    // Only update status if this is still the current task
    const currentEditingView = selectCurrentEditingView(
      getState(),
    ) as ViewViewModel;
    if (currentEditingView && currentEditingView.currentTaskId === taskId) {
      updateTaskStatusWithoutProgress(
        dispatch,
        taskId,
        SqlTaskStatus.FAILED,
        errorMsg,
      );
      dispatch(
        viewActions.changeCurrentEditingView({
          stage: ViewViewModelStages.Initialized,
          error: errorMsg,
        }),
      );
    }

    throw error;
  }
});

export const saveView = createAsyncThunk<
  ViewViewModel,
  SaveViewParams,
  { state: RootState }
>('view/saveView', async ({ resolve, isSaveAs, currentView }, { getState }) => {
  let currentEditingView = isSaveAs
    ? (currentView as ViewViewModel)
    : (selectCurrentEditingView(getState()) as ViewViewModel);
  const orgId = selectOrgId(getState());
  const allDatabaseSchemas = selectAllSourceDatabaseSchemas(getState());

  const transformResponse = (currentView, data, isSaveAs) => {
    return {
      ...currentView,
      ...data,
      config: currentView.config,
      model: currentView.model,
      variables: (data.variables || []).map(v => ({
        ...v,
        relVariableSubjects: data.relVariableSubjects,
      })),
      isSaveAs,
    };
  };

  if (isNewView(currentEditingView.id) || isSaveAs) {
    const { data } = await request2<View>({
      url: '/views',
      method: 'POST',
      data: getSaveParamsFromViewModel(
        orgId,
        currentEditingView,
        false,
        allDatabaseSchemas[currentEditingView.sourceId!],
        isSaveAs,
      ),
    });
    resolve && resolve();
    return transformResponse(currentEditingView, data, isSaveAs);
  } else {
    const { data } = await request2<View>({
      url: `/views/${currentEditingView.id}`,
      method: 'PUT',
      data: getSaveParamsFromViewModel(
        orgId,
        currentEditingView,
        true,
        allDatabaseSchemas[currentEditingView.sourceId!],
        isSaveAs,
      ),
    });
    resolve && resolve();
    return transformResponse(currentEditingView, data, isSaveAs);
  }
});

export const saveFolder = createAsyncThunk<
  ViewSimple,
  SaveFolderParams,
  { state: RootState }
>('view/saveFolder', async ({ folder, resolve }, { getState }) => {
  const orgId = selectOrgId(getState());
  if (!(folder as ViewSimple).id) {
    const { data } = await request2<View>({
      url: '/views',
      method: 'POST',
      data: { orgId, isFolder: true, ...folder },
    });
    resolve && resolve();
    return data;
  } else {
    await request2<View>({
      url: `/views/${(folder as ViewSimple).id}`,
      method: 'PUT',
      data: folder,
    });
    resolve && resolve();
    return folder as ViewSimple;
  }
});

export const updateViewBase = createAsyncThunk<ViewBase, UpdateViewBaseParams>(
  'view/updateViewBase',
  async ({ view, resolve }) => {
    await request2<boolean>({
      url: `/views/${view.id}/base`,
      method: 'PUT',
      data: view,
    });
    resolve();
    return view;
  },
);

export const removeEditingView = createAsyncThunk<
  null,
  { id: string; resolve: (currentEditingViewKey: string) => void },
  { state: RootState }
>('view/removeEditingView', async ({ id, resolve }, { dispatch, getState }) => {
  dispatch(viewActions.removeEditingView(id));
  const currentEditingViewKey = selectCurrentEditingViewKey(getState());
  resolve(currentEditingViewKey);
  return null;
});

export const closeOtherEditingViews = createAsyncThunk<
  null,
  { id: string; resolve: (currentEditingViewKey: string) => void },
  { state: RootState }
>(
  'view/closeOtherEditingViews',
  async ({ id, resolve }, { dispatch, getState }) => {
    dispatch(viewActions.closeOtherEditingViews(id));
    const currentEditingViewKey = selectCurrentEditingViewKey(getState());
    resolve(currentEditingViewKey);
    return null;
  },
);

export const closeAllEditingViews = createAsyncThunk<
  null,
  { resolve: () => void },
  { state: RootState }
>('view/closeAllEditingViews', async ({ resolve }, { dispatch, getState }) => {
  dispatch(viewActions.closeAllEditingViews());
  resolve();
  return null;
});

export const unarchiveView = createAsyncThunk<
  string,
  UnarchiveViewParams,
  { state: RootState }
>(
  'view/unarchiveView',
  async ({ view: { id, name, parentId, index }, resolve }, { dispatch }) => {
    try {
      await request2<null>({
        url: `/views/unarchive/${id}`,
        method: 'PUT',
        params: { name, parentId, index },
      });
      resolve();
      return id;
    } catch (error) {
      errorHandle(error);
      throw error;
    }
  },
);

export const deleteView = createAsyncThunk<
  null,
  DeleteViewParams,
  { state: RootState }
>('view/deleteView', async ({ id, archive, resolve }, { dispatch }) => {
  await request2<boolean>({
    url: `/views/${id}`,
    method: 'DELETE',
    params: { archive },
  });
  resolve();
  return null;
});

export const getEditorProvideCompletionItems = createAsyncThunk<
  null,
  { sourceId?: string; resolve: (getItems: any) => void },
  { state: RootState }
>(
  'view/getEditorProvideCompletionItems',
  ({ sourceId, resolve }, { getState }) => {
    const variables = selectCurrentEditingViewAttr(getState(), {
      name: 'variables',
    }) as VariableHierarchy[];
    const publicVariables = selectVariables(getState());

    const dbKeywords = new Set<string>();
    const tableKeywords = new Set<string>();
    const schemaKeywords = new Set<string>();
    const variableKeywords = new Set<string>();

    if (sourceId) {
      const currentDBSchemas = selectSourceDatabaseSchemas(getState(), {
        id: sourceId,
      });
      currentDBSchemas?.forEach(db => {
        if (db.dbName) {
          dbKeywords.add(db.dbName);
        }
        db.tables?.forEach(table => {
          if (table.tableName) {
            tableKeywords.add(table.tableName);
          }
          table.columns?.forEach(column => {
            if (column.name) {
              schemaKeywords.add(column.name as string);
            }
          });
        });
      });
    }

    ([] as Array<VariableHierarchy | Variable>)
      .concat(variables)
      .concat(publicVariables)
      .forEach(({ name }) => {
        if (name) {
          variableKeywords.add(name);
        }
      });

    const getItems = (model, position) => {
      const word = model.getWordUntilPosition(position);
      const range = {
        startLineNumber: position.lineNumber,
        endLineNumber: position.lineNumber,
        startColumn: word.startColumn,
        endColumn: word.endColumn,
      };
      const dataSource = [
        { detail: 'SQL', keywords: sqlReservedWords },
        { detail: 'Database', keywords: Array.from(dbKeywords) },
        { detail: 'Table', keywords: Array.from(tableKeywords) },
        { detail: 'Column', keywords: Array.from(schemaKeywords) },
        { detail: 'Variable', keywords: Array.from(variableKeywords) },
      ];
      return {
        suggestions: dataSource
          .filter(({ keywords }) => !!keywords)
          .reduce<monaco.languages.CompletionItem[]>(
            (arr, { detail, keywords }) => {
              const validKeywords = keywords!.filter(
                str => typeof str === 'string' && str,
              );
              return arr.concat(
                validKeywords.map(str => ({
                  label: str,
                  detail,
                  kind: monaco.languages.CompletionItemKind.Keyword,
                  insertText: detail === 'Variable' ? `$${str}$` : str,
                  range,
                })),
              );
            },
            [],
          ),
      };
    };

    resolve(getItems);

    return null;
  },
);
