$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Base = 'https://raw.githubusercontent.com/cloudflare/security-audit-skill/main/skills/security-audit'
$Files = @(
'AI-AND-LLM.md','ATTACK-CLASSES.md','CLIENT-SIDE.md','CLOUD-AND-DEPLOYMENT.md',
'DATA-ISOLATION-AND-LIFECYCLE.md','DESKTOP-MOBILE-AND-LOCAL-IPC.md','HUNTING.md',
'MEMORY-SAFETY-AND-BINARY.md','PROTOCOLS-RPC-AND-MESSAGING.md','RECONNAISSANCE.md',
'RESOURCE-EXHAUSTION-AND-AVAILABILITY.md','SKILL.md','SUPPLY-CHAIN-AND-RELEASE.md',
'VALIDATION-AND-REPORTING.md','WEB-PROTOCOL-AND-AUTH.md','report-schema.json',
'validate-coverage-ledger.cjs','validate-coverage-ledger.test.cjs','validate-findings.cjs','validate-findings.test.cjs'
)
foreach ($File in $Files) {
  Invoke-WebRequest -Uri "$Base/$File" -OutFile (Join-Path $Root $File)
}
Write-Host 'Cloudflare security-audit-skill synced from main.'
