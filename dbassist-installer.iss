; DBAssist Installer Script for Inno Setup
; Download Inno Setup from: https://jrsoftware.org/isdl.php

#define MyAppName "DBAssist"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Masaddat Mallick"
#define MyAppURL "https://github.com/masaddat/dbassist"
#define MyAppExeName "DBAssist-Launcher.bat"
#define MyAppEmail "masaddat.mallick@gmail.com"

[Setup]
; Application Information
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
AppCopyright=Copyright (C) 2026 {#MyAppPublisher}
AppComments=AI-Powered Database Assistant for SQL Server and Oracle

; Installation Directories
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes

; License
LicenseFile=LICENSE.txt

; Output
OutputDir=installer\output
OutputBaseFilename=DBAssist-Setup-{#MyAppVersion}
Compression=lzma2/max
SolidCompression=yes

; Privileges
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog

; Icons and Images
SetupIconFile=src\main\resources\icon.ico
WizardStyle=modern

; Other
UninstallDisplayIcon={app}\{#MyAppExeName}
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Application Files
Source: "target\DBAssist.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "DBAssist-Launcher.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme
Source: "LICENSE.txt"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Start Menu
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

; Desktop Icon
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; Option to launch after installation
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
function InitializeSetup(): Boolean;
var
  JavaInstalled: Boolean;
  JavaVersion: String;
  ResultCode: Integer;
  ErrorMsg: String;
begin
  Result := True;
  JavaInstalled := False;

  // Check if Java is installed by trying to run java -version
  if Exec('cmd.exe', '/c java -version 2>&1', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    if ResultCode = 0 then
      JavaInstalled := True;
  end;

  if not JavaInstalled then
  begin
    ErrorMsg := 'Java Runtime Environment (JRE) 17 or higher is required to run DBAssist.' + #13#10 + #13#10 +
                'Java is not installed or not in your system PATH.' + #13#10 + #13#10 +
                'Please install Java before continuing:' + #13#10 +
                '1. Visit https://adoptium.net/' + #13#10 +
                '2. Download JRE 17 or higher' + #13#10 +
                '3. Install Java' + #13#10 +
                '4. Run this installer again' + #13#10 + #13#10 +
                'Click OK to visit the Java download page, or Cancel to exit installation.';

    if MsgBox(ErrorMsg, mbConfirmation, MB_OKCANCEL) = IDOK then
    begin
      ShellExec('open', 'https://adoptium.net/', '', '', SW_SHOWNORMAL, ewNoWait, ResultCode);
    end;

    Result := False;
  end
  else
  begin
    // Java is installed, show info message
    MsgBox('Java Runtime Environment detected.' + #13#10 +
           'DBAssist will be installed now.', mbInformation, MB_OK);
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
  begin
    // Create a desktop shortcut manually if needed
    // Additional post-installation tasks can be added here
  end;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;

  // Additional validation can be added here for each page
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usPostUninstall then
  begin
    // Cleanup tasks after uninstallation
    // Remove configuration files if needed
  end;
end;

