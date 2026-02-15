# SQL Worksheet - Visual Reference Guide

## UI Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  📝 ConnectionName - Worksheet                            [X]   │
├─────────────────────────────────────────────────────────────────┤
│  TOOLBAR                                                         │
│  [▶ Execute] [⚡ Format] [💬 Comment] [🗑 Clear]                │
│  [📊 Open in Grid]                    📁 ConnectionName         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  SQL EDITOR (White Background, Bold Text)                       │
│  ┌────────────────────────────────────────────────────────────┐│
│  │ 1  SELECT * FROM Customers                                 ││
│  │ 2  WHERE City = 'London'                                   ││
│  │ 3  ORDER BY CustomerName                                   ││
│  │                                                            ││
│  │    ↑                                                       ││
│  │    Keywords in BLUE                                        ││
│  │    Strings in RED                                          ││
│  │    All text BOLD                                           ││
│  └────────────────────────────────────────────────────────────┘│
│                                                                  │
├═════════════════════════════════════════════════════════════════┤
│  RESULTS PANE                                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ [Results 1] [Results 2]                                  │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ Query: SELECT * FROM...                                  │  │
│  │                                                           │  │
│  │ ┌─────────────┬──────────────┬─────────────┐           │  │
│  │ │ CustomerID  │ CustomerName │ City        │           │  │
│  │ ├─────────────┼──────────────┼─────────────┤           │  │
│  │ │ 1           │ John Doe     │ London      │           │  │
│  │ │ 2           │ Jane Smith   │ London      │           │  │
│  │ └─────────────┴──────────────┴─────────────┘           │  │
│  └──────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Ready                                                           │
└─────────────────────────────────────────────────────────────────┘
```

## Color Scheme

### SQL Editor
- **Background**: Pure white (#ffffff)
- **Text**: Dark gray, bold (#2c3e50)
- **Current Line**: Very light gray (#f6f8fa)

### Syntax Highlighting
```sql
SELECT CustomerID, CustomerName, City    -- SELECT, FROM are BLUE
FROM Customers                            -- Keywords BOLD
WHERE City = 'London'                     -- 'London' is RED
  AND Active = 1                          -- Numbers are DARK BLUE
-- This is a comment                      -- Comments are GREEN, italic
```

### Auto-Complete Menu
```
┌─────────────────────┐
│ Customers          │  ← Dark gray text (#2c3e50)
│ CustomersArchive   │  ← Bold, 13px font
│ CustomerTypes      │  ← White background
└─────────────────────┘  ← Gray border
```

## Toolbar Buttons

### Button States

#### ▶ Execute (Always enabled)
- **Color**: Green (#27ae60)
- **Action**: Run query (Ctrl+Enter)

#### ⚡ Format (Always enabled)
- **Color**: Blue (#3498db)
- **Action**: Auto-format SQL

#### 💬 Comment (Always enabled)
- **Color**: Gray (#95a5a6)
- **Action**: Toggle comments

#### 🗑 Clear (Always enabled)
- **Color**: Red (#e74c3c)
- **Action**: Clear all text

#### 📊 Open in Grid (Conditional)
- **Color**: Purple (#9b59b6)
- **Disabled**: Gray (when no results)
- **Enabled**: After successful SELECT query
- **Action**: Open results in Table Grid tab

## Workflow Diagram

```
┌─────────────────┐
│  Write Query    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Press Execute  │
└────────┬────────┘
         │
         ├──────────────────┬──────────────────┐
         │                  │                  │
         ▼                  ▼                  ▼
   ┌──────────┐      ┌──────────┐      ┌──────────┐
   │  SELECT  │      │   DML    │      │  ERROR   │
   └────┬─────┘      └────┬─────┘      └────┬─────┘
        │                 │                  │
        ▼                 ▼                  ▼
   Show Results     Show Row Count     Show Error
   Enable Button    Disable Button    Disable Button
        │
        ▼
   Click "Open in Grid"
        │
        ▼
   New Table Grid Tab
```

## Example Scenarios

### Scenario 1: Simple SELECT Query
```
1. Type: SELECT * FROM Products
2. Press Ctrl+Enter
3. See results in bottom pane
4. "📊 Open in Grid" button turns purple (enabled)
5. Click button
6. New tab opens: "Query Result - ConnectionName"
7. Can now filter, compare, clone the results
```

### Scenario 2: UPDATE Query
```
1. Type: UPDATE Products SET Price = Price * 1.1
2. Press Ctrl+Enter
3. See message: "3 row(s) affected"
4. "📊 Open in Grid" button stays gray (disabled)
5. No automatic tab creation
```

### Scenario 3: Query with Error
```
1. Type: SELECT * FROM NonExistentTable
2. Press Ctrl+Enter
3. See error in red tab
4. "📊 Open in Grid" button stays gray (disabled)
5. No error tabs created automatically
```

### Scenario 4: Multiple Queries
```
1. Execute: SELECT * FROM Products
2. Results show, button enabled
3. Execute: SELECT * FROM Customers
4. New results show, button still enabled
5. Click button → Opens LAST query (Customers)
6. To open Products, re-execute that query
```

## Keyboard Shortcuts

| Action              | Shortcut      | Result                    |
|---------------------|---------------|---------------------------|
| Execute Query       | Ctrl+Enter    | Runs current/selected SQL |
| Auto-Complete       | Ctrl+Space    | Shows suggestions         |
| Format SQL          | (Button)      | Auto-formats code         |
| Comment Line        | (Button)      | Adds/removes --           |

## Tips for Best Visual Experience

### 1. Font Readability
- Text is bold by default for clarity
- Use Consolas or Courier New fonts
- 14px size is optimal for most screens

### 2. Syntax Colors
- Blue keywords stand out clearly
- Red strings are easy to spot
- Green comments don't distract
- Dark background text ensures readability

### 3. Auto-Complete
- Dark text on white background
- High contrast ratio
- Easy to read even in bright light

### 4. Results Viewing
- Bottom pane for quick preview
- Grid tab for detailed analysis
- Choose based on your workflow

## Common Questions

**Q: Can I change the theme to dark?**
A: Currently light theme only. Dark theme may be added in future.

**Q: Why is text bold?**
A: Bold text improves readability and reduces eye strain for long coding sessions.

**Q: Can I customize syntax colors?**
A: Currently colors are fixed. Customization may be added in future.

**Q: How do I make auto-complete text bigger?**
A: Font size is 13px by default. Can be increased in future versions.

**Q: Why doesn't the grid open automatically?**
A: To prevent errors and give you control. Click the button when needed.

## Visual Indicators

### Button States
- 🟢 Green = Action button (Execute)
- 🔵 Blue = Formatting (Format)
- 🟣 Purple = Navigation (Open in Grid)
- 🔴 Red = Destructive (Clear)
- ⚫ Gray = Utility (Comment)

### Text Colors in Editor
- 🔵 Blue = Keywords (SELECT, FROM, WHERE)
- 🔴 Red = Strings ('text values')
- 🟢 Green = Comments (-- comments)
- 🔷 Dark Blue = Numbers (123, 45.67)
- ⚫ Dark Gray = Everything else

### Menu Indicators
- White background = Clean, professional
- Gray border = Clear boundaries
- Dark text = High readability
- Bold font = Easy to scan

## Print Reference

```
SYNTAX COLORS:
- Keywords:  BLUE (#0000ff) BOLD
- Strings:   RED (#d73a49) BOLD
- Comments:  GREEN (#22863a) ITALIC
- Numbers:   DARK BLUE (#005cc5) BOLD
- Regular:   DARK GRAY (#2c3e50) BOLD

TOOLBAR:
- Execute:      GREEN   (Always On)
- Format:       BLUE    (Always On)
- Comment:      GRAY    (Always On)
- Clear:        RED     (Always On)
- Open in Grid: PURPLE  (After SELECT)

SHORTCUTS:
- Execute:      Ctrl + Enter
- Auto-Complete: Ctrl + Space
```

---

**Note**: This visual reference is based on the light theme design. All colors and layouts are optimized for clarity and professional appearance.

