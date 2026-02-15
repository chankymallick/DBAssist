# SQL Worksheet - Quick Start Guide

## Opening a Worksheet

1. **From Connection Tree**:
   - Right-click on any database connection
   - Select "📝 New SQL Worksheet"
   - A new worksheet tab opens

## Writing SQL Queries

### Auto-Completion
- **Trigger**: Press `Ctrl + Space` or just start typing
- **Table Names**: Type 2+ characters to see table suggestions
- **Column Names**: Type `TableName.` to see column suggestions
- **Keywords**: All SQL keywords are available

### Example:
```sql
SELECT * FROM Customers WHERE

-- As you type "Cust", suggestions appear
-- After typing "Customers.", column suggestions appear
```

## Executing Queries

### Method 1: Execute All
1. Write your query
2. Press `Ctrl + Enter`
3. Results appear in the bottom pane

### Method 2: Execute Selection
1. Select part of your query
2. Press `Ctrl + Enter`
3. Only selected portion executes

### Method 3: Use Button
- Click the green "▶ Execute" button in the toolbar

## Toolbar Features

### ▶ Execute (Ctrl+Enter)
Runs the current query or selected text

### ⚡ Format
Auto-formats your SQL for better readability:
- Places keywords on new lines
- Adds proper indentation
- Improves code structure

### 💬 Comment
- Select lines to comment
- Click to add `--` prefix
- Click again to uncomment

### 🗑 Clear
Clears the entire worksheet content

### 📊 Open in Grid
- Opens the last successful SELECT query result in a Table Grid tab
- Button is enabled only after a successful SELECT query execution
- Allows you to compare query results with other tables
- Grid tab supports all standard features (filters, column selection, etc.)

## Query Results

### SELECT Queries
- Results display in a table at the bottom
- Each execution creates a new result tab
- Click "📊 Open in Grid" to open results in a separate Table Grid tab (for comparison/analysis)

### DML Queries (INSERT, UPDATE, DELETE)
- Shows number of rows affected
- Displays success message
- "Open in Grid" button is disabled for DML queries

### Errors
- Displayed in red in an error tab
- Shows full error message for debugging

## Tips & Tricks

### 1. Use Auto-Completion
```sql
-- Type "SEL" and press Ctrl+Space
SELECT * FROM ...

-- Type table name and dot
Customers.  -- Shows all columns
```

### 2. Execute Multiple Queries
```sql
-- Select only the query you want to run
SELECT * FROM Orders WHERE OrderDate > '2024-01-01';

-- Or run all queries (Ctrl+Enter without selection)
```

### 3. Format for Readability
Before:
```sql
SELECT CustomerID,CustomerName,City FROM Customers WHERE City='London' ORDER BY CustomerName
```

After clicking "⚡ Format":
```sql
SELECT CustomerID,CustomerName,City 
FROM Customers 
WHERE City='London' 
ORDER BY CustomerName
```

### 4. Quick Comments
```sql
-- Select these lines and click "💬 Comment"
SELECT * FROM Orders
WHERE Status = 'Pending'

-- Result:
-- SELECT * FROM Orders
-- WHERE Status = 'Pending'
```

### 5. Open Results in Grid for Analysis
1. Execute a SELECT query
2. View results in the bottom pane
3. Click "📊 Open in Grid" button to open in a Table Grid tab
4. Now you can:
   - Apply filters to the data
   - Select specific columns to view
   - Compare with other table data using "⚖ Compare Tables"
   - Clone the tab to create variations

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Execute Query | `Ctrl + Enter` |
| Auto-Complete | `Ctrl + Space` |
| Save (future) | `Ctrl + S` |

## Syntax Highlighting

The editor highlights:
- **Keywords**: Blue and bold (SELECT, FROM, WHERE)
- **Strings**: Orange ('text values')
- **Comments**: Green and italic (-- comments)
- **Numbers**: Light green (123, 45.67)

## Common Queries

### Select All Records
```sql
SELECT * FROM TableName
```

### Filtered Select
```sql
SELECT * FROM Customers
WHERE City = 'London'
```

### Join Tables
```sql
SELECT o.OrderID, c.CustomerName
FROM Orders o
INNER JOIN Customers c ON o.CustomerID = c.CustomerID
```

### Count Records
```sql
SELECT COUNT(*) as TotalOrders
FROM Orders
WHERE OrderDate > '2024-01-01'
```

### Update Records
```sql
UPDATE Products
SET Price = Price * 1.1
WHERE Category = 'Electronics'
```

### Insert Records
```sql
INSERT INTO Customers (CustomerName, City, Country)
VALUES ('New Customer', 'London', 'UK')
```

## Best Practices

1. **Test with SELECT**: Always test with SELECT before UPDATE/DELETE
2. **Use Comments**: Document complex queries
3. **Format Regularly**: Use the Format button to keep code clean
4. **Save Important Queries**: Copy to external files (built-in save coming soon)
5. **Use Auto-Complete**: Speeds up writing and reduces typos

## Troubleshooting

### Auto-Completion Not Working
- Press `Ctrl + Space` manually
- Type at least 2 characters
- Ensure worksheet is connected to a database

### Query Not Executing
- Check for SQL syntax errors
- Verify connection is active
- Look at error tab for details

### No Results Displayed
- Check if query returns data
- Verify connection has data
- Look for filters that might hide results

## Connection Information

The worksheet shows:
- Connection name in the tab title
- Connection name in the toolbar
- All tables/columns from that connection

## Future Features (Coming Soon)

- Save queries to disk
- Query history
- Multiple statement execution
- Query templates
- Performance metrics
- Direct export from worksheet

## Support

For issues or feature requests, check the documentation or contact support at masaddat.mallick@gmail.com

