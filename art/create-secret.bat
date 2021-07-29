@echo off

rd /s /q Work
md Work
xcopy ..\app\purchase Work\_tmp\purchase /E /C /I
rd /s /q Work\_tmp\purchase\build
rd /s /q Work\_tmp\purchase\src\androidTest
rd /s /q Work\_tmp\purchase\src\test

tar -C Work\_tmp -cvzf Work\protect.tar.gz *

certutil -encode Work\protect.tar.gz Work\encode-p.txt && findstr /v /c:- Work\encode-p.txt > Work\encode.txt

del /s /q Work\encode-p.txt

echo.
echo.
echo Upload the secrets "encode.txt" for Github action, see if you want to
echo backup the "protect.tar.gz" as well.
echo.
pause