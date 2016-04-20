Find has a couple of "HPE Find" logos.  Let's look at how to change them.

# 1. Find the image files

Navigate to [core/src/main/public/static/img](https://github.com/hpe-idol/find/tree/master/core/src/main/public/static/img)

You will see several `.png` image files.  Open them up and familiarize yourself with which are which.

# 2. Replace the image files

Replace the image files with the `.png` images that you want to use as logos instead.  Try to keep the image dimensions as close to the originals as possible.

# 3. Build Find and Test your changes

Follow the steps in [[Running a Development Copy of Find]].  Load Find in a web browser and check that your changes work.  You might need to force refresh your browser (often `ctrl+F5`) if the changes don't immediately appear.

# 4. Adjust the styling

If the logos don't display correctly, you might need to adjust the CSS.

Navigate to [core/src/main/public/static/css](https://github.com/hpe-idol/find/tree/master/core/src/main/public/static/css) and load `app-include.css`.  Search in the file for the filenames of the images that you have replaced, e.g. `Find_Logo_lge.png`.  This will bring up the rules that control the display of the images.  Adjust the `width` and `height` to match the dimensions of the image.