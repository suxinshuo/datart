import styled from 'styled-components/macro';

const TableWrapper = styled.div`
  margin: 12px 0;
  overflow-x: auto;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
`;

const Table = styled.table`
  width: 100%;
  font-size: 14px;
  border-collapse: collapse;
  background-color: #ffffff;
`;

const TableHead = styled.thead`
  background-color: #f5f8fa;
  border-bottom: 2px solid #e4e6ef;
`;

const TableBody = styled.tbody`
  tr:nth-child(even) {
    background-color: #f5f8fa;
  }
`;

const TableRow = styled.tr`
  border-bottom: 1px solid #e4e6ef;

  &:last-child {
    border-bottom: none;
  }
`;

const TableHeaderCell = styled.th`
  padding: 10px 12px;
  font-weight: 600;
  color: #3f4254;
  text-align: left;
  white-space: nowrap;
  vertical-align: top;
`;

const TableCell = styled.td`
  padding: 10px 12px;
  line-height: 1.5;
  color: #3f4254;
  text-align: left;
  vertical-align: top;
`;

export const MarkdownTable = Object.assign(
  ({ children }: { children: React.ReactNode }) => {
    return (
      <TableWrapper>
        <Table>{children}</Table>
      </TableWrapper>
    );
  },
  {
    Head: TableHead,
    Body: TableBody,
    Row: TableRow,
    HeaderCell: TableHeaderCell,
    Cell: TableCell,
  },
);
