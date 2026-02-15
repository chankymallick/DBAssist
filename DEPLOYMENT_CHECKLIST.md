# 🚀 GitHub Pages Deployment Checklist

## Pre-Deployment

### ✅ Files to Include in Repository

1. **Landing Page**
   - [x] `index.html` - Main landing page
   - [x] `UIScreenShots/` folder - All 5 screenshots

2. **Documentation**
   - [x] `README.md` - Project README (use README_PROJECT.md)
   - [x] `GITHUB_PAGES_SETUP.md` - Setup instructions
   - [x] `LICENSE.txt` - License file

3. **Build Files** (upload to Releases)
   - [ ] Windows Installer (from `target/windows-installer/`)
   - [ ] Portable JAR (from `target/portable-jar/`)
   - [ ] Linux Package (from `target/linux-package/`)

---

## Deployment Steps

### Step 1: Prepare Repository
```bash
# Create new repository on GitHub: "DBAssistant"
# Clone to local machine
git clone https://github.com/YOUR_USERNAME/DBAssistant.git
cd DBAssistant
```

### Step 2: Copy Files
```bash
# Copy essential files
cp index.html DBAssistant/
cp -r UIScreenShots DBAssistant/
cp README_PROJECT.md DBAssistant/README.md
cp GITHUB_PAGES_SETUP.md DBAssistant/
cp LICENSE.txt DBAssistant/
```

### Step 3: Commit and Push
```bash
git add .
git commit -m "Initial commit: DBAssistant landing page"
git push origin main
```

### Step 4: Enable GitHub Pages
1. Go to repository **Settings**
2. Navigate to **Pages** (left sidebar)
3. Under **Source**:
   - Branch: `main`
   - Folder: `/ (root)`
4. Click **Save**
5. Wait 2-3 minutes for deployment

### Step 5: Create Release
1. Go to **Releases** → **Create a new release**
2. Tag: `v1.0.0`
3. Title: `DBAssistant v1.0.0 - Initial Release`
4. Description:
   ```markdown
   ## DBAssistant v1.0.0
   
   Initial release of DBAssistant - AI-Powered Database Explorer
   
   ### Features
   - SQL Worksheet with syntax highlighting
   - Smart data comparison
   - Advanced filtering
   - Auto-save functionality
   - Connection management
   - Export to Excel/HTML
   
   ### Download Options
   - Windows Installer (.exe)
   - Portable JAR (.zip)
   - Linux Package (.zip)
   
   ### System Requirements
   - Java 17 or higher
   - Windows 10+, Linux, or macOS
   - SQL Server 2012+ or Oracle 11g+
   
   ### Installation
   See README.md for detailed instructions.
   ```

5. Upload build files:
   - `DBAssistant-Setup.exe`
   - `DBAssistant-Portable.zip`
   - `DBAssistant-Linux.zip`

6. Check **Set as the latest release**
7. Click **Publish release**

### Step 6: Update Download Links in index.html

Replace placeholder links with actual release URLs:

```html
<!-- Windows Installer -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Setup.exe" 
   class="btn btn-primary">
    Download Setup (.exe)
</a>

<!-- Portable JAR -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Portable.zip" 
   class="btn btn-primary">
    Download JAR
</a>

<!-- Linux Package -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Linux.zip" 
   class="btn btn-primary">
    Download (.zip)
</a>
```

Commit and push:
```bash
git add index.html
git commit -m "Update download links with actual release URLs"
git push origin main
```

### Step 7: Verify Deployment
1. Visit: `https://YOUR_USERNAME.github.io/DBAssistant/`
2. Check all sections load correctly
3. Test screenshot modal
4. Verify download links work
5. Test on mobile device

---

## Post-Deployment

### ✅ Verification Checklist

**Website**
- [ ] Landing page loads correctly
- [ ] All images display properly
- [ ] Navigation links work
- [ ] Screenshot modal opens/closes
- [ ] Download buttons point to correct URLs
- [ ] Contact information is correct
- [ ] Responsive on mobile
- [ ] No console errors

**Repository**
- [ ] README displays properly
- [ ] All documentation files included
- [ ] License file present
- [ ] Release created successfully
- [ ] Download files available

**SEO & Discoverability**
- [ ] Repository has description
- [ ] Repository has topics/tags
- [ ] GitHub Pages site listed in About section
- [ ] Social preview image set (optional)

---

## Optimization Tips

### Add Repository Description
1. Go to repository home page
2. Click ⚙️ (gear icon) next to About
3. Add:
   - **Description**: "AI-Powered Database Explorer for SQL Server and Oracle"
   - **Website**: `https://YOUR_USERNAME.github.io/DBAssistant/`
   - **Topics**: `database`, `sql-server`, `oracle`, `javafx`, `database-explorer`, `sql-tool`

### Add Social Preview Image
1. Go to repository **Settings**
2. Scroll to **Social preview**
3. Upload an image (recommended: 1280x640px)
4. Use a screenshot or create a banner

### Enable GitHub Features
- [ ] Enable **Issues** for bug reports
- [ ] Enable **Discussions** for community
- [ ] Add **Contributing Guidelines** (CONTRIBUTING.md)
- [ ] Add **Code of Conduct** (CODE_OF_CONDUCT.md)

---

## Maintenance

### Updating the Website

1. **Edit files locally**
   ```bash
   git pull origin main
   # Edit index.html or other files
   git add .
   git commit -m "Update: description of changes"
   git push origin main
   ```

2. **Changes go live automatically** (2-3 minutes)

### Creating New Releases

1. Build new version
2. Create new tag: `v1.1.0`
3. Upload new build files
4. Update version in `index.html`
5. Update download links if needed

---

## Custom Domain (Optional)

To use a custom domain like `dbassistant.dev`:

1. **Buy domain** from registrar (Namecheap, GoDaddy, etc.)

2. **Add CNAME file** to repository:
   ```
   dbassistant.dev
   ```

3. **Configure DNS** at registrar:
   ```
   Type: CNAME
   Host: www
   Value: YOUR_USERNAME.github.io
   ```

4. **Update GitHub Pages settings**:
   - Go to Settings → Pages
   - Enter custom domain
   - Enable HTTPS

---

## Troubleshooting

### Site Not Loading
- Wait 5-10 minutes after enabling Pages
- Clear browser cache
- Check GitHub Pages deployment status
- Ensure `index.html` is in root directory

### Images Not Displaying
- Verify UIScreenShots folder is in repository
- Check image paths are correct
- Ensure file names match (case-sensitive)

### Download Links Broken
- Verify release is published
- Check URL format is correct
- Ensure files were uploaded to release

### 404 Error
- Verify GitHub Pages is enabled
- Check branch and folder settings
- Ensure repository is public

---

## Support

If you encounter issues:
1. Check [GitHub Pages Documentation](https://docs.github.com/en/pages)
2. Review this checklist again
3. Contact: masaddat.mallick@gmail.com

---

## URLs to Update

Before going live, update these in `index.html`:

- [ ] `github.com/YOUR_USERNAME/DBAssistant` → Actual repo URL
- [ ] `YOUR_USERNAME.github.io/DBAssistant` → Actual site URL
- [ ] Download links → Actual release URLs
- [ ] LinkedIn URL → Actual profile URL
- [ ] Documentation links → Actual doc URLs

---

## Final Checklist

- [ ] Repository created on GitHub
- [ ] Files uploaded and committed
- [ ] GitHub Pages enabled
- [ ] Release created with downloads
- [ ] Download links updated
- [ ] Repository description added
- [ ] Topics/tags added
- [ ] Website verified working
- [ ] Mobile responsiveness tested
- [ ] All links tested
- [ ] Contact info verified
- [ ] README displays correctly

---

**Once completed, your professional landing page will be live!** 🎉

**Live URL**: `https://YOUR_USERNAME.github.io/DBAssistant/`

---

Last Updated: February 15, 2026

