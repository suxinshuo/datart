import styled from 'styled-components/macro';

const TaskListItem = styled.li`
  display: flex;
  gap: 8px;
  align-items: flex-start;
  margin: 4px 0;
  line-height: 1.6;
  color: #3f4254;
  list-style-type: none;
`;

const TaskListCheckbox = styled.input`
  flex-shrink: 0;
  margin: 4px 0 0 0;
  cursor: pointer;
  accent-color: #1b9aee;
`;

export const MarkdownTaskList = {
  Item: TaskListItem,
  Checkbox: TaskListCheckbox,
};
