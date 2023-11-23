@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  app startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and APP_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\app.jar;%APP_HOME%\lib\guava-30.0-jre.jar;%APP_HOME%\lib\camel-core-3.5.0.jar;%APP_HOME%\lib\camel-core-languages-3.5.0.jar;%APP_HOME%\lib\camel-file-3.5.0.jar;%APP_HOME%\lib\camel-cluster-3.5.0.jar;%APP_HOME%\lib\camel-health-3.5.0.jar;%APP_HOME%\lib\camel-xml-jaxb-3.5.0.jar;%APP_HOME%\lib\camel-core-engine-3.5.0.jar;%APP_HOME%\lib\camel-base-3.5.0.jar;%APP_HOME%\lib\camel-bean-3.5.0.jar;%APP_HOME%\lib\camel-browse-3.5.0.jar;%APP_HOME%\lib\camel-caffeine-lrucache-3.5.0.jar;%APP_HOME%\lib\camel-controlbus-3.5.0.jar;%APP_HOME%\lib\camel-dataformat-3.5.0.jar;%APP_HOME%\lib\camel-direct-3.5.0.jar;%APP_HOME%\lib\camel-directvm-3.5.0.jar;%APP_HOME%\lib\camel-language-3.5.0.jar;%APP_HOME%\lib\camel-log-3.5.0.jar;%APP_HOME%\lib\camel-dataset-3.5.0.jar;%APP_HOME%\lib\camel-mock-3.5.0.jar;%APP_HOME%\lib\camel-ref-3.5.0.jar;%APP_HOME%\lib\camel-rest-3.5.0.jar;%APP_HOME%\lib\camel-saga-3.5.0.jar;%APP_HOME%\lib\camel-scheduler-3.5.0.jar;%APP_HOME%\lib\camel-stub-3.5.0.jar;%APP_HOME%\lib\camel-vm-3.5.0.jar;%APP_HOME%\lib\camel-seda-3.5.0.jar;%APP_HOME%\lib\camel-timer-3.5.0.jar;%APP_HOME%\lib\camel-validator-3.5.0.jar;%APP_HOME%\lib\camel-xpath-3.5.0.jar;%APP_HOME%\lib\camel-xslt-3.5.0.jar;%APP_HOME%\lib\camel-xml-jaxp-3.5.0.jar;%APP_HOME%\lib\camel-support-3.5.0.jar;%APP_HOME%\lib\camel-core-catalog-3.5.0.jar;%APP_HOME%\lib\camel-api-3.5.0.jar;%APP_HOME%\lib\camel-management-api-3.5.0.jar;%APP_HOME%\lib\camel-util-3.5.0.jar;%APP_HOME%\lib\slf4j-api-1.7.30.jar;%APP_HOME%\lib\commons-dbcp2-2.9.0.jar;%APP_HOME%\lib\commons-pool2-2.11.1.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.5.0.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\caffeine-2.8.5.jar;%APP_HOME%\lib\camel-tooling-model-3.5.0.jar;%APP_HOME%\lib\jakarta.xml.bind-api-2.3.3.jar;%APP_HOME%\lib\jaxb-core-2.3.0.jar;%APP_HOME%\lib\jaxb-impl-2.3.0.jar;%APP_HOME%\lib\camel-util-json-3.5.0.jar;%APP_HOME%\lib\jakarta.activation-api-1.2.2.jar


@rem Execute app
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %APP_OPTS%  -classpath "%CLASSPATH%" repairer.App %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable APP_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%APP_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
