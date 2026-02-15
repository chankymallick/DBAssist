# DBAssistant - GitHub Pages Setup

## Deploying to GitHub Pages

Follow these steps to publish the DBAssistant landing page on GitHub Pages:

### Step 1: Upload Files to GitHub Repository

1. Create a new GitHub repository (or use existing one)
2. Upload the following files to the repository root:
   - `index.html`
   - `UIScreenShots/` folder with all screenshots

### Step 2: Enable GitHub Pages

1. Go to your repository on GitHub
2. Click **Settings** tab
3. Scroll down to **Pages** section (left sidebar)
4. Under **Source**, select:
   - Branch: `main` (or `master`)
   - Folder: `/ (root)`
5. Click **Save**

### Step 3: Update Download Links

Once you upload the build files to GitHub, update the download links in `index.html`:

```html
<!-- Replace the href="#" with actual download URLs -->

<!-- Windows Installer -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Setup.exe" class="btn btn-primary">
    Download Setup (.exe)
</a>

<!-- Portable JAR -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Portable.zip" class="btn btn-primary">
    Download JAR
</a>

<!-- Linux Package -->
<a href="https://github.com/YOUR_USERNAME/DBAssistant/releases/download/v1.0.0/DBAssistant-Linux.zip" class="btn btn-primary">
    Download (.zip)
</a>
```

### Step 4: Create a Release

1. Go to **Releases** in your repository
2. Click **Create a new release**
3. Tag: `v1.0.0`
4. Title: `DBAssistant v1.0.0`
5. Upload the build files:
   - Windows installer (from `target/windows-installer/`)
   - Portable JAR (from `target/portable-jar/`)
   - Linux package (from `target/linux-package/`)
6. Publish the release

### Step 5: Access Your Site

Your site will be available at:
```
https://YOUR_USERNAME.github.io/DBAssistant/
```

Wait a few minutes for GitHub to build and deploy the site.

## Repository Structure

```
DBAssistant/
├── index.html                          (landing page)
├── UIScreenShots/                      (screenshots folder)
│   ├── Screenshot 2026-02-15 100701.png
│   ├── Screenshot 2026-02-15 100806.png
│   ├── Screenshot 2026-02-15 100857.png
│   ├── Screenshot 2026-02-15 102222.png
│   └── Screenshot 2026-02-15 102304.png
├── README.md                           (project readme)
└── GITHUB_PAGES_SETUP.md              (this file)
```

## Features of the Landing Page

### Professional Design ✅
- Modern gradient hero section
- Responsive layout
- Smooth animations
- Clean typography

### Sections ✅
1. **Navigation** - Sticky header with quick links
2. **Hero** - Eye-catching introduction with CTA buttons
3. **Features** - 9 feature cards with icons
4. **Screenshots** - Gallery with modal view
5. **Download** - Multiple download options
6. **System Requirements** - Clear requirements grid
7. **Contact** - Multiple contact methods
8. **Footer** - Social links and credits

### Interactive Elements ✅
- Smooth scroll navigation
- Screenshot modal (click to enlarge)
- Hover effects on all cards
- Animated elements on scroll
- Responsive mobile menu

### Contact Information Included ✅
- Email: masaddat.mallick@gmail.com
- GitHub: github.com/masaddat
- LinkedIn: linkedin.com/in/masaddat
- Issue tracker link

## Customization

### Update Contact Links
Search for these placeholders in `index.html` and update:
- `github.com/masaddat/DBAssistant`
- `linkedin.com/in/masaddat`
- GitHub repository URL

### Add More Screenshots
1. Add images to `UIScreenShots/` folder
2. Add new screenshot items in the gallery:
```html
<div class="screenshot-item" onclick="openModal('UIScreenShots/your-image.png')">
    <img src="UIScreenShots/your-image.png" alt="Description">
    <div class="screenshot-caption">Your Caption</div>
</div>
```

### Update Features
Edit the feature cards in the Features section to add/remove features.

### Change Color Scheme
Update CSS variables in `:root`:
```css
:root {
    --primary-color: #2c3e50;
    --secondary-color: #3498db;
    --accent-color: #27ae60;
    /* ... other colors */
}
```

## Testing Locally

To test the page locally before deploying:

1. Open `index.html` in a web browser
2. Or use a local server:
   ```bash
   # Python 3
   python -m http.server 8000
   
   # Then visit: http://localhost:8000
   ```

## SEO Optimization

The page includes:
- ✅ Meta description
- ✅ Meta keywords
- ✅ Semantic HTML5
- ✅ Alt tags on images
- ✅ Descriptive titles

## Mobile Responsive

The page is fully responsive:
- ✅ Mobile-first design
- ✅ Flexible grid layouts
- ✅ Adaptive navigation
- ✅ Touch-friendly buttons

## Browser Compatibility

Tested and working on:
- ✅ Chrome/Edge (latest)
- ✅ Firefox (latest)
- ✅ Safari (latest)
- ✅ Mobile browsers

## Performance

Optimized for speed:
- ✅ No external dependencies
- ✅ Inline CSS (no extra requests)
- ✅ Optimized images
- ✅ Minimal JavaScript
- ✅ Fast load time

## Support

For issues with the website:
- Create an issue on GitHub
- Email: masaddat.mallick@gmail.com

## License

The landing page code is part of the DBAssistant project and follows the same license (MIT).

---

**Your professional landing page is ready to deploy!** 🚀

