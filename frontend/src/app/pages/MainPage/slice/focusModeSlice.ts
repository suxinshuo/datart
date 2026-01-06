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

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { useInjectReducer } from 'utils/@reduxjs/injectReducer';

// 定义专注模式状态类型
export interface FocusModeState {
  isFocusMode: boolean;
}

// 初始状态
export const initialState: FocusModeState = {
  isFocusMode: false,
};

const slice = createSlice({
  name: 'focusMode',
  initialState,
  reducers: {
    // 切换到专注模式
    enterFocusMode(state) {
      state.isFocusMode = true;
    },
    // 退出专注模式
    exitFocusMode(state) {
      state.isFocusMode = false;
    },
    // 切换专注模式状态
    toggleFocusMode(state) {
      state.isFocusMode = !state.isFocusMode;
    },
  },
});

// 导出actions
export const { enterFocusMode, exitFocusMode, toggleFocusMode } = slice.actions;

// 导出hook用于注入reducer
export const useFocusModeSlice = () => {
  useInjectReducer({ key: slice.name, reducer: slice.reducer });
  return { actions: slice.actions };
};

// 导出reducer
export default slice.reducer;