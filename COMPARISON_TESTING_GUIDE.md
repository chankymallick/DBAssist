# Comparison Feature Testing Guide

## To Test the Column Selection Issue

### Step 1: Open Two Table Tabs
1. In the left tree, expand a connection (e.g., "MyConnection")
2. Expand "Tables"
3. Right-click on a table (e.g., "Customers")
4. Select "Fetch Data"
5. **Wait for the data to load completely**
6. Repeat for the same table from another connection OR the same table from same connection

### Step 2: Initiate Comparison
1. Click the "⚖ Compare Tables" button in the top header bar
2. Dialog should open

### Step 3: Select Tabs
1. Select Source Tab from dropdown
2. Select Target Tab from dropdown
3. **Check console output for debug messages:**
   ```
   Tab: Connection - TableName - Visible columns: X - [column1, column2, ...]
   updateColumnSelection called - Source: ...
   Common columns found: X
   Added X column checkboxes
   ```

### Step 4: What Should Happen
After selecting both tabs, you should see:
- "Step 2: Select columns for row identification" label
- Helper text about uniquely identifying rows
- A scrollable box with checkboxes for each common column
- "Select All" and "Clear All" buttons

### What to Check in Console

Look for these debug messages:

**When clicking Compare Tables:**
```
getVisibleColumns called - Total columns: X
  Column visible: ColumnName1
  Column visible: ColumnName2
  ...
Returning X visible columns: [...]
Tab: Connection - Table - Visible columns: X - [...]
```

**When selecting both tabs:**
```
updateColumnSelection called - Source: Table, Target: Table
Common columns found: X
Added X column checkboxes
Column selection UI visibility set to true
```

### If Step 2 is Still Empty

**Possible Issues:**

1. **No columns found**
   - Console shows: "Returning 0 visible columns"
   - **Fix:** Make sure table data has loaded before comparing

2. **Columns found but not showing**
   - Console shows: "Added X column checkboxes"
   - But UI not visible
   - **Check:** ScrollPane visibility

3. **Different table names**
   - Validation error showing
   - Must be exactly same table name

### Debug Console Messages to Look For

```
✓ Good:
getVisibleColumns called - Total columns: 5
  Column visible: CustomerID
  Column visible: Name
  Column visible: Email
  ...
Returning 5 visible columns: [CustomerID, Name, Email, ...]

updateColumnSelection called - Source: Customers, Target: Customers
Common columns found: 5
Added 5 column checkboxes
Column selection UI visibility set to true

✗ Problem:
getVisibleColumns called - Total columns: 5
  Column visible: NULL NAME  ← Problem here!
Returning 0 visible columns: []

updateColumnSelection called - Source: Customers, Target: Customers
Common columns found: 0  ← No columns!
```

### Send Me This Information

If still not working, copy and send:

1. **Console output** when you click "Compare Tables"
2. **Console output** after selecting both tabs
3. **Screenshot** of the dialog
4. **Table name** you're trying to compare

This will help me identify exactly what's happening!

