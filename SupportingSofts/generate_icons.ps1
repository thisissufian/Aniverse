Add-Type -AssemblyName System.Drawing

$icoPath = "F:\cool projects\Aniverse\build\icon.ico"
$resDir = "F:\cool projects\Aniverse\android\app\src\main\res"

Write-Host "Reading $icoPath..."

# Load icon using System.Drawing.Icon
$fileStream = [System.IO.File]::OpenRead($icoPath)
$icon = [System.Drawing.Icon]::FromHandle(([System.Drawing.Icon]::new($fileStream, 256, 256)).Handle)
$bitmap = $icon.ToBitmap()

Write-Host "Loaded bitmap: $($bitmap.Width)x$($bitmap.Height)"

$densities = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($entry in $densities.GetEnumerator()) {
    $folder = Join-Path $resDir $entry.Key
    if (-not (Test-Path $folder)) {
        New-Item -ItemType Directory -Path $folder -Force | Out-Null
    }
    
    $size = $entry.Value
    $targetBmp = New-Object System.Drawing.Bitmap $size, $size
    $graphics = [System.Drawing.Graphics]::FromImage($targetBmp)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.DrawImage($bitmap, 0, 0, $size, $size)
    
    $outPath = Join-Path $folder "ic_launcher.png"
    $targetBmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    # Also save ic_launcher_round.png
    $roundPath = Join-Path $folder "ic_launcher_round.png"
    $targetBmp.Save($roundPath, [System.Drawing.Imaging.ImageFormat]::Png)

    $graphics.Dispose()
    $targetBmp.Dispose()
    Write-Host "Generated: $outPath ($size x $size)"
}

# Also generate a large 512x512 foreground icon for adaptive icon
$fgSize = 512
$fgBmp = New-Object System.Drawing.Bitmap $fgSize, $fgSize
$gFg = [System.Drawing.Graphics]::FromImage($fgBmp)
$gFg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$gFg.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$gFg.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

# Center the icon with some padding for adaptive icon safe zone (safe zone is inner 66% ~ 72dp of 108dp)
# 512 * (72 / 108) = ~341px
$innerSize = [int]($fgSize * 0.70)
$offset = [int](($fgSize - $innerSize) / 2)
$gFg.DrawImage($bitmap, $offset, $offset, $innerSize, $innerSize)

$drawableDir = Join-Path $resDir "drawable"
$fgPath = Join-Path $drawableDir "ic_launcher_foreground.png"
$fgBmp.Save($fgPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Generated adaptive foreground: $fgPath"

$gFg.Dispose()
$fgBmp.Dispose()
$bitmap.Dispose()
$fileStream.Close()
$fileStream.Dispose()

Write-Host "All Android icons generated successfully!"
