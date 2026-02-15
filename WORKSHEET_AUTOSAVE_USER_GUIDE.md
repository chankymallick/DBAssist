# SQL Worksheet Auto-Save - User Guide

## What's New? 🎉

Your SQL Worksheets now **automatically save as you type**! Never lose your work again.

## How It Works

### Completely Automatic
- **No "Save" button needed**
- **No manual action required**
- **Just type and close**

### Real-Time Saving
```
You type: SELECT * FROM Customers
  ↓ (automatically)
Saved to disk immediately
  ↓
You close the tab
  ↓
Content is preserved
  ↓
You reopen worksheet
  ↓
All your queries are back! ✅
```

## Features

### 1. Auto-Save on Every Keystroke ⌨️
- Saves as you type
- No delays or lag
- Works in background
- Completely silent

### 2. Automatic Restoration 🔄
- Close worksheet → Content saved
- Restart application → Content restored
- Open new worksheet for same connection → Previous content loads
- Zero configuration needed

### 3. Cross-Session Persistence 💾
- Survives app restarts
- Survives computer restarts
- Survives crashes
- Content is always safe

## Usage

### Opening a Worksheet
```
1. Right-click connection in tree
2. Select "📝 New SQL Worksheet"
3. Worksheet opens
4. If you had previous content → It loads automatically!
5. If new → Starts empty
```

### Working with Queries
```
1. Type your SQL query
   SELECT * FROM Customers WHERE City = 'London'
   
2. (Automatically saved in background)

3. Execute query (Ctrl+Enter)

4. Keep typing more queries
   SELECT * FROM Orders WHERE OrderDate > '2024-01-01'
   
5. (Automatically saved again)

6. Close tab whenever you want
```

### Reopening Later
```
1. Close worksheet tab (or entire app)
2. Later: Right-click same connection
3. Select "📝 New SQL Worksheet"
4. Your previous content is there! ✅
```

## What Gets Saved

### Everything in the Editor:
- ✅ All SQL queries
- ✅ Comments
- ✅ Blank lines
- ✅ Formatting
- ✅ Everything you type

### What Doesn't Get Saved:
- ❌ Query execution results (these are temporary)
- ❌ Cursor position
- ❌ Selected text
- ❌ Undo history

## Storage Location

### Where Are My Worksheets Saved?

**Windows:**
```
C:\Users\YourName\.dbassist\worksheets\
```

**Linux:**
```
/home/yourname/.dbassist/worksheets/
```

**Mac:**
```
/Users/yourname/.dbassist/worksheets/
```

### File Format:
- Plain text `.sql` files
- Can be opened in any text editor
- UTF-8 encoding
- Standard SQL format

### File Names:
```
ConnectionName_Timestamp.sql

Examples:
- MyDatabase_1739563201234.sql
- Production_Server_1739563301456.sql
- Dev_Oracle_1739563401789.sql
```

## Examples

### Example 1: Daily Work
```
Monday 9 AM:
  - Open worksheet
  - Write queries for customer analysis
  - Execute and check results
  - Close app for lunch

Monday 2 PM:
  - Open worksheet again
  - All morning queries still there! ✅
  - Continue working
```

### Example 2: Long Query Development
```
Day 1:
  - Start writing complex JOIN query
  - Get partway through
  - Need to leave for meeting
  - Just close the tab

Day 2:
  - Open worksheet
  - Complex query is still there! ✅
  - Continue from where you left off
```

### Example 3: Multiple Connections
```
- Open worksheet for Production DB
  Write some queries
  
- Open worksheet for Development DB
  Write different queries
  
- Close both

- Later: Open Production worksheet
  → Production queries appear
  
- Open Development worksheet
  → Development queries appear
  
Each connection has its own saved content! ✅
```

## Tips & Tricks

### Tip 1: Use Comments
```sql
-- Customer Analysis - 2024-02-15
-- This query finds customers in London
SELECT * FROM Customers WHERE City = 'London'

-- Order History Query
-- Gets all orders from last month
SELECT * FROM Orders WHERE OrderDate > '2024-01-01'
```
Comments are saved too, so you can organize your queries!

### Tip 2: Keep Multiple Versions
```sql
-- Version 1 (old)
-- SELECT * FROM Customers

-- Version 2 (current)
SELECT CustomerID, CustomerName, City FROM Customers
WHERE City = 'London'
```
Comment out old versions instead of deleting them.

### Tip 3: Build Query Library
```sql
-- === USEFUL QUERIES ===

-- Top 10 Customers by Orders
SELECT TOP 10 CustomerID, COUNT(*) as OrderCount
FROM Orders GROUP BY CustomerID ORDER BY OrderCount DESC

-- Recent Orders
SELECT * FROM Orders WHERE OrderDate > DATEADD(day, -7, GETDATE())

-- Customer Details with Order Count
SELECT c.*, COUNT(o.OrderID) as Orders
FROM Customers c LEFT JOIN Orders o ON c.CustomerID = o.CustomerID
GROUP BY c.CustomerID, c.CustomerName
```
Build a library of frequently used queries!

### Tip 4: Use Multiple Worksheets
- Each new worksheet gets its own file
- You can have multiple worksheets per connection
- Organize by purpose (analysis, reports, maintenance)

## FAQ

### Q: Do I need to click "Save"?
**A:** No! Saving is automatic.

### Q: What if I close without saving?
**A:** It's already saved! You can close anytime.

### Q: Can I have multiple worksheets?
**A:** Yes! Each new worksheet is saved separately.

### Q: What happens if app crashes?
**A:** Content is safe! Last typed text is saved.

### Q: Can I access the files manually?
**A:** Yes! They're in `~/.dbassist/worksheets/` as `.sql` files.

### Q: Can I share my queries?
**A:** Yes! Copy the `.sql` file from the worksheets folder.

### Q: How much space do worksheets use?
**A:** Very little! Typical query: 1-10 KB per file.

### Q: Can I delete old worksheets?
**A:** Yes! Just delete the `.sql` files from the worksheets folder.

### Q: Does this slow down typing?
**A:** No! Saving happens in background. Zero lag.

### Q: What if disk is full?
**A:** App keeps working, saves to memory cache.

## Backup Your Worksheets

### Manual Backup:
**Windows:**
```
Copy folder: C:\Users\YourName\.dbassist\worksheets
To: External drive or cloud storage
```

**Linux/Mac:**
```bash
cp -r ~/.dbassist/worksheets ~/backup/worksheets
```

### Recommended:
- Backup monthly if worksheets are important
- Or include in your regular backup routine

## Troubleshooting

### Issue: My queries aren't saving
1. Check disk space (need at least 10 MB free)
2. Check permissions on `~/.dbassist/worksheets/`
3. Look for error messages in console

### Issue: Content didn't load
1. Check if file exists in worksheets folder
2. Try opening file in text editor to verify
3. Reopen worksheet tab

### Issue: Slow typing
1. Check number of worksheets (>1000?)
2. Check file sizes (>1 MB?)
3. Contact support if issue persists

## Advanced

### Viewing Your Worksheets:
```bash
# Linux/Mac
cd ~/.dbassist/worksheets
ls -lh

# See your saved queries
cat MyDatabase_1739563201234.sql
```

### Version Control:
You can version control your worksheets:
```bash
cd ~/.dbassist/worksheets
git init
git add *.sql
git commit -m "My SQL query library"
```

### Sharing Queries:
1. Navigate to worksheets folder
2. Copy the `.sql` file
3. Share via email, chat, etc.
4. Others can open in any text editor

## What's Next?

Future enhancements may include:
- Manual save button (for those who prefer)
- Save delay configuration
- Query history/versions
- Auto-cleanup old files
- Cloud sync

---

## Summary

**Before:**
- ❌ Manual save required
- ❌ Easy to lose work
- ❌ Forget to save and lose hours of queries

**Now:**
- ✅ Automatic saving
- ✅ Never lose work
- ✅ Just type and close
- ✅ Content always preserved

**Enjoy hassle-free SQL query development!** 🚀

