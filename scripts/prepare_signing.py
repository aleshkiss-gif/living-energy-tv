import base64,json,os
from pathlib import Path
bundle=json.loads(os.environ['SIGNING_BUNDLE'])
password=bundle['password'];alias=bundle['alias']
# Reject characters that could inject Java properties or workflow log commands.
assert all(ch.isalnum() or ch in '-_' for ch in password) and len(password)>=24
assert alias=='living-energy'
key=base64.b64decode(bundle['keystore'],validate=True)
assert 1000<len(key)<100000
Path('release.jks').write_bytes(key);os.chmod('release.jks',0o600)
Path('signing.properties').write_text(f'storeFile=release.jks\nstorePassword={password}\nkeyPassword={password}\nkeyAlias={alias}\n')
os.chmod('signing.properties',0o600)
print('Stable signing configured')
