# build.ps1,  Version 0.01
# Compile HPE Find in Windows
#
# Usage:  ./build.ps1
#
# Written by Yuntaz - http://www.yuntaz.com

$java = &"java.exe" -version 2>&1
$java = $java[0].tostring().Substring(0,4)

$ErrorActionPreference= 'silentlycontinue'
$nodejs = (node -v).Substring(1,5)
$maven = (mvn --version).Substring(0,12)[0]

if ($nodejs -eq '5.1.0' -And $maven -eq 'Apache Maven' -And $java -eq 'java')
{
	Write-Host "Compiling ... "
	cd webapp
	&mvn clean package -Pproduction -pl idol,hod -am  
}
else
{
	Write-Host "Prerequisites are not present on the system. "
	Write-Host "Please, check that Java is installed and on the PATH."
	Write-Host "Please, check that NodeJs is installed on version 5.1.0. "
	Write-Host "Please, check that Apache Maven is installed and running on the PATH"
}

