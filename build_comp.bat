@echo off
echo -------------------------------- Building --------------------------------
set /p Input=Enter Enter Version Number:
cd ..
echo Backing up src
XCOPY forge\mcp\src forge\mcp\src-bak /E /I /Q /y
echo.
echo Copying source 
XCOPY "jas\src" "forge\mcp\src\minecraft" /E /Q /y
XCOPY "jas\src_comp" "forge\mcp\src\minecraft" /E /Q /y
echo.
echo Recompile
pushd forge\mcp
echo | call recompile.bat
echo Done.
echo.
echo Reobfuscate
echo | call reobfuscate_srg.bat
echo Done.
popd
echo.

echo Copy Mod Code into Mod Module in Setup 
XCOPY forge\mcp\reobf\minecraft\jas\compatability forge\mcp\reobf\minecraft\SETUP\JustAnotherSpawner\jas\compatability /E /I /Q /y

echo Move Active into Setup
pushd forge\mcp\reobf\minecraft\SETUP
echo Using 7Zip to Zip Mod
"C:\Program Files\7-zip\7z.exe" a JustAnotherSpawnerCompatability%Input%.zip .\JustAnotherSpawner\* -r | findstr /b /r /c:"\<Everything is Ok" /c:"\<Scanning" /c:"\<Creating archive"
popd

echo Restoring src-bak
RMDIR /S /Q forge\mcp\src
REN forge\mcp\src-bak src
PAUSE
