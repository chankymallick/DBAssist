# 🚀 Quick Start - Publish DBAssistant Website

## 5-Minute Deployment Guide

### Step 1: Create GitHub Repository (2 minutes)
1. Go to https://github.com/new
2. Repository name: `DBAssistant`
3. Description: `AI-Powered Database Explorer for SQL Server and Oracle`
4. Public repository
5. Click **Create repository**

### Step 2: Upload Files (2 minutes)
Upload these files to your repository:
- ✅ `index.html`
- ✅ `UIScreenShots/` folder (all 5 images)
- ✅ `README_PROJECT.md` (rename to `README.md`)
- ✅ `LICENSE.txt`
- ✅ `GITHUB_PAGES_SETUP.md`

**Via GitHub Web Interface:**
1. Click "uploading an existing file"
2. Drag and drop files
3. Commit changes

**Or via Git:**
```bash
git clone https://github.com/YOUR_USERNAME/DBAssistant.git
cd DBAssistant
cp /path/to/index.html .
cp -r /path/to/UIScreenShots .
cp /path/to/README_PROJECT.md README.md
git add .
git commit -m "Add landing page and assets"
git push origin main
```

### Step 3: Enable GitHub Pages (1 minute)
1. Go to repository **Settings**
2. Click **Pages** (left sidebar)
3. Source: Branch `main`, Folder `/ (root)`
4. Click **Save**
5. Wait 2-3 minutes

### Done! 🎉
Your website will be live at:
```
https://YOUR_USERNAME.github.io/DBAssistant/
```

---

## Optional: Add Download Links

### Step 4: Create Release (3 minutes)
1. Go to **Releases** → **Draft a new release**
2. Tag: `v1.0.0`
3. Title: `DBAssistant v1.0.0`
4. Upload your build files:
   - Windows installer
   - Portable JAR
   - Linux package
5. Click **Publish release**

### Step 5: Update Download Links (2 minutes)
Edit `index.html` and replace `href="#"` with actual URLs:

```html
<!-- Example: -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Setup.exe" 
   class="btn btn-primary">
    Download Setup (.exe)
</a>
```

Commit and push:
```bash
git add index.html
git commit -m "Update download links"
git push origin main
```

---

## Test Your Website

Visit your live site and check:
- ✅ Page loads correctly
- ✅ All images display
- ✅ Navigation works
- ✅ Screenshots enlarge on click
- ✅ Download buttons work (if added)
- ✅ Mobile responsive

---

## Customize (Optional)

### Update Repository Info
1. Go to repository home
2. Click ⚙️ next to "About"
3. Add:
   - **Website**: Your GitHub Pages URL
   - **Topics**: `database`, `sql-server`, `oracle`, `javafx`

### Update Contact Links
In `index.html`, search and replace:
- `YOUR_USERNAME` → Your GitHub username
- `masaddat` → Your username (if different)
- Update LinkedIn, email as needed

---

## Need Help?

**Full Guide**: See `DEPLOYMENT_CHECKLIST.md`  
**Setup Instructions**: See `GITHUB_PAGES_SETUP.md`  
**Questions**: masaddat.mallick@gmail.com

---

## Files Checklist

Files to upload:
- [x] `index.html` - Landing page
- [x] `UIScreenShots/` - 5 screenshot images
- [x] `README_PROJECT.md` → `README.md` - Project docs
- [x] `LICENSE.txt` - License file
- [x] `GITHUB_PAGES_SETUP.md` - Setup guide (optional)

Build files (for releases):
- [ ] Windows installer
- [ ] Portable JAR
- [ ] Linux package

---

**That's it! Your professional website is now live!** 🚀

