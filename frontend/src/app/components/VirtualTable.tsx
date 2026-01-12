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

import { Empty, Table, TableProps, Tooltip } from 'antd';
import classNames from 'classnames';
import { TABLE_DATA_INDEX } from 'globalConstants';
import React, {
  memo,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { VariableSizeGrid as Grid } from 'react-window';
import styled from 'styled-components/macro';
import { SPACE_TIMES, SPACE_XS } from 'styles/StyleConstants';

const CellContent = styled.div`
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border-bottom: 1px solid ${p => p.theme.borderColorSplit};
`;

const TooltipContent = styled.div`
  display: flex;
  gap: ${SPACE_XS};
  align-items: flex-start;
  max-width: 600px;
  word-break: break-all;
`;

const TooltipText = styled.div`
  flex: 1;
  max-width: 550px;
`;

interface TooltipCellProps {
  value: any;
  style: React.CSSProperties;
  className?: string;
}

const TooltipCell = memo(({ value, style, className }: TooltipCellProps) => {
  const [isOverflowing, setIsOverflowing] = useState(false);
  const textRef = useRef<HTMLDivElement>(null);

  const displayValue = value === null ? 'NULL' : value;
  const textValue = String(displayValue);
  const isNull = value === null;

  useEffect(() => {
    if (textRef.current) {
      const { scrollWidth, clientWidth } = textRef.current;
      setIsOverflowing(scrollWidth > clientWidth);
    }
  }, [textValue]);

  const cellContent = (
    <CellContent
      ref={textRef}
      className={className}
      style={{
        ...style,
        color: isNull ? '#999' : style.color,
      }}
    >
      {displayValue}
    </CellContent>
  );

  if (isOverflowing) {
    return (
      <Tooltip
        title={
          <TooltipContent>
            <TooltipText>{textValue}</TooltipText>
          </TooltipContent>
        }
        placement="topLeft"
        overlayStyle={{ maxWidth: '600px' }}
      >
        {cellContent}
      </Tooltip>
    );
  }

  return cellContent;
});

TooltipCell.displayName = 'TooltipCell';

interface VirtualTableProps extends TableProps<object> {
  width: number;
  scroll: { x: number; y: number };
  columns: any;
}

/**
 * Table组件中使用了虚拟滚动条 渲染的速度变快 基于（react-windows）
 * 使用方法：import { VirtualTable } from 'app/components/VirtualTable';
 * <VirtualTable
    dataSource={dataSourceWithKey}
    columns={columns}
    width={width}
    ...tableProps
  />
 */
export const VirtualTable = memo((props: VirtualTableProps) => {
  const { columns, scroll, width: boxWidth, dataSource } = props;
  const gridRef: any = useRef();
  const [connectObject] = useState(() => {
    const obj = {};
    Object.defineProperty(obj, 'scrollLeft', {
      get: () => null,
      set: scrollLeft => {
        if (gridRef.current) {
          gridRef.current.scrollTo({
            scrollLeft,
          });
        }
      },
    });
    return obj;
  });

  // 当columns变化时，重新计算widthColumns和widthColumnCount
  const widthColumns = useMemo(() => {
    return columns.map(v => {
      return { width: v.width, dataIndex: v.dataIndex };
    });
  }, [columns]);

  const widthColumnCount = useMemo(() => {
    return widthColumns.filter(
      ({ width, dataIndex }) => !width || dataIndex !== TABLE_DATA_INDEX,
    ).length;
  }, [widthColumns]);

  const mergedColumns = useMemo(() => {
    const isFull = boxWidth > scroll.x;
    const updatedWidthColumns = [...widthColumns];

    if (isFull === true) {
      updatedWidthColumns.forEach((v, i) => {
        updatedWidthColumns[i].width =
          updatedWidthColumns[i].dataIndex === TABLE_DATA_INDEX
            ? updatedWidthColumns[i].width
            : updatedWidthColumns[i].width +
              (boxWidth - scroll.x) / widthColumnCount;
      });
    }

    return columns.map((column, i) => {
      return {
        ...column,
        width: column.width
          ? updatedWidthColumns[i].width
          : Math.floor(boxWidth / widthColumnCount),
      };
    });
  }, [boxWidth, columns, scroll.x, widthColumnCount, widthColumns]);

  const resetVirtualGrid = useCallback(() => {
    gridRef.current?.resetAfterIndices({
      columnIndex: 0,
      shouldForceUpdate: true,
    });
  }, [gridRef]);

  // 优化重置逻辑，只有在真正需要时才重置
  useEffect(() => {
    // 只有当列宽或数据发生显著变化时才重置
    if (columns.length > 0 && dataSource && dataSource.length > 0) {
      resetVirtualGrid();
    }
  }, [boxWidth, columns.length, dataSource?.length, resetVirtualGrid]);

  const renderVirtualList = useCallback(
    (rawData, { scrollbarSize, ref, onScroll }) => {
      ref.current = connectObject;
      const totalHeight = rawData.length * 39;

      if (!dataSource?.length) {
        //If the data is empty
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />;
      }

      return (
        <Grid
          ref={gridRef}
          className="virtual-grid"
          columnCount={mergedColumns.length}
          columnWidth={index => {
            const { width } = mergedColumns[index];
            return totalHeight > scroll.y && index === mergedColumns.length - 1
              ? width - scrollbarSize - 16
              : width;
          }}
          height={scroll.y}
          rowCount={rawData.length}
          rowHeight={() => 39}
          width={boxWidth}
          // 优化overscanCount，减少预渲染的行数和列数，平衡滚动流畅度和性能
          overscanCount={3}
          overscanColumnCount={3}
          onScroll={({ scrollLeft }) => {
            onScroll({
              scrollLeft,
            });
          }}
        >
          {({ rowIndex, columnIndex, style }) => {
            const cellValue =
              rawData[rowIndex][mergedColumns[columnIndex].dataIndex];
            const cellStyle = {
              padding: `${SPACE_TIMES(2)}`,
              textAlign: mergedColumns[columnIndex].align,
              ...style,
            };
            const cellClassName = classNames('virtual-table-cell', {
              'virtual-table-cell-last':
                columnIndex === mergedColumns.length - 1,
            });
            return (
              <TooltipCell
                value={cellValue}
                style={cellStyle}
                className={cellClassName}
                key={columnIndex}
              />
            );
          }}
        </Grid>
      );
    },
    [mergedColumns, boxWidth, connectObject, dataSource, scroll],
  );

  return (
    <Table
      {...props}
      columns={mergedColumns}
      components={{
        body: renderVirtualList,
        ...props.components,
      }}
    />
  );
});
