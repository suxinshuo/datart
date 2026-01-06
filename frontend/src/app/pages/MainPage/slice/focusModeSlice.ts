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

// 从localStorage读取初始状态，默认值为true
const getInitialState = (): FocusModeState => {
  try {
    const savedState = localStorage.getItem('datart_focus_mode');
    if (savedState !== null) {
      return JSON.parse(savedState);
    }
  } catch (error) {
    console.error('Failed to read focus mode state from localStorage:', error);
  }
  return {
    isFocusMode: true,
  };
};

// 初始状态
export const initialState: FocusModeState = getInitialState();

const saveFocusModeState = (state: FocusModeState) => {
  try {
    localStorage.setItem('datart_focus_mode', JSON.stringify(state));
  } catch (error) {
    console.error('Failed to save focus mode state to localStorage:', error);
  }
};

const slice = createSlice({
  name: 'focusMode',
  initialState,
  reducers: {
    // 切换到专注模式
    enterFocusMode(state) {
      state.isFocusMode = true;
      saveFocusModeState(state);
    },
    // 退出专注模式
    exitFocusMode(state) {
      state.isFocusMode = false;
      saveFocusModeState(state);
    },
    // 切换专注模式状态
    toggleFocusMode(state) {
      state.isFocusMode = !state.isFocusMode;
      saveFocusModeState(state);
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