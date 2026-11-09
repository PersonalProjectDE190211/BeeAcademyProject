param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ArgsList
)

$nodePath = "D:\Vo_Van_Thanh_Dat\Check\node.exe"
$vitePath = Join-Path $PSScriptRoot "node_modules\vite\bin\vite.js"

if ($ArgsList -contains "dev" -or ($ArgsList[0] -eq "run" -and $ArgsList[1] -eq "dev")) {
    & $nodePath $vitePath --port=3000 --host=0.0.0.0 --strictPort
} else {
    & $nodePath $vitePath @ArgsList
}

