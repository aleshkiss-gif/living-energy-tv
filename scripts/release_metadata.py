import hashlib,json,re,os
from pathlib import Path
source=Path('app/build.gradle').read_text()
code=int(re.search(r'versionCode\s+(\d+)',source).group(1))
name=re.search(r"versionName\s+'([^']+)'",source).group(1)
apk=Path('dist/LivingEnergy.apk')
assert apk.is_file()
meta={'versionCode':code,'versionName':name,'minSdk':26,'apkUrl':f'https://github.com/aleshkiss-gif/living-energy-tv/releases/download/v{code}/LivingEnergy.apk','sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'size':apk.stat().st_size}
Path('dist/update.json').write_text(json.dumps(meta,indent=2)+'\n')
with open(os.environ['GITHUB_OUTPUT'],'a') as f:f.write(f'tag=v{code}\nversion={name}\n')
