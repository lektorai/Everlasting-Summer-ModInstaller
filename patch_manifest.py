#!/usr/bin/env python3
import sys
from pathlib import Path

p = Path(sys.argv[1])
s = p.read_text(encoding='utf-8')
needle = '<application '
if 'ModPickerActivity' in s:
    raise SystemExit(0)
idx = s.find(needle)
if idx < 0:
    raise SystemExit('application tag not found')
# Insert an activity immediately after the opening application tag.
end = s.find('>', idx)
if end < 0:
    raise SystemExit('application opening tag is malformed')
activity = '\n        <activity android:name="su.sovietgames.everlasting_summer.ModPickerActivity" android:exported="false" />'
s = s[:end+1] + activity + s[end+1:]
p.write_text(s, encoding='utf-8')
