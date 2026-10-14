from PIL import Image,ImageDraw
from pathlib import Path
import random,json,functools
@functools.lru_cache(None)
def patterns(n,clues):
 out=[]
 def place(k,start,mask):
  if k==len(clues):out.append(mask);return
  tail=sum(clues[k+1:])+max(0,len(clues)-k-1)
  for x in range(start,n-clues[k]-tail+1):place(k+1,x+clues[k]+1,mask|(((1<<clues[k])-1)<<x))
 if clues:place(0,0,0)
 else:out=[0]
 return out

def runs(a):
 out=[];k=0
 for v in a+[0]:
  if v:k+=1
  elif k:out.append(k);k=0
 return tuple(out)

def logic(s,n):
 lines=[patterns(n,runs(s[r*n:(r+1)*n]))[:] for r in range(n)]+[patterns(n,runs([s[r*n+c] for r in range(n)]))[:] for c in range(n)]
 board=[-1]*(n*n)
 for step in range(n*n):
  changed=False
  for l,opts in enumerate(lines):
   ids=[l*n+k for k in range(n)] if l<n else [k*n+l-n for k in range(n)]
   ones=sum((1<<k) for k,i in enumerate(ids) if board[i]==1);zeros=sum((1<<k) for k,i in enumerate(ids) if board[i]==0)
   opts=[v for v in opts if v&ones==ones and v&zeros==0]
   if not opts:return False
   lines[l]=opts;a=(1<<n)-1;b=0
   for v in opts:a&=v;b|=v
   for k,i in enumerate(ids):
    v=1 if a>>k&1 else 0 if not(b>>k&1) else -1
    if v!=-1 and board[i]==-1:board[i]=v;changed=True
  if not changed:break
 return board==s

def picture(kind,n):
 im=Image.new('1',(100,100));d=ImageDraw.Draw(im)
 if kind=='owl':
  d.ellipse((22,18,78,85),fill=1);d.polygon([(22,38),(22,8),(42,23)],fill=1);d.polygon([(78,38),(78,8),(58,23)],fill=1);d.ellipse((28,26,48,46),fill=0);d.ellipse((52,26,72,46),fill=0);d.ellipse((35,31,42,40),fill=1);d.ellipse((58,31,65,40),fill=1);d.polygon([(43,46),(57,46),(50,57)],fill=0);d.rectangle((28,86,39,92),fill=1);d.rectangle((61,86,72,92),fill=1)
 elif kind=='key':
  d.ellipse((20,5,70,50),fill=1);d.ellipse((32,15,58,36),fill=0);d.rectangle((39,43,51,90),fill=1);d.rectangle((48,66,71,77),fill=1);d.rectangle((48,83,71,93),fill=1)
 elif kind=='ship':
  d.rectangle((48,8,54,76),fill=1);d.polygon([(43,15),(43,61),(10,61)],fill=1);d.polygon([(60,19),(60,62),(85,62)],fill=1);d.polygon([(9,70),(92,70),(76,90),(25,90)],fill=1)
 elif kind=='hourglass':
  d.rectangle((18,6,82,15),fill=1);d.rectangle((18,84,82,94),fill=1);d.polygon([(26,15),(74,15),(57,48),(57,53),(74,84),(26,84),(43,53),(43,48)],fill=1);d.polygon([(37,22),(63,22),(50,40)],fill=0);d.polygon([(50,63),(64,80),(36,80)],fill=0)
 elif kind=='crown':
  d.polygon([(13,24),(32,48),(50,10),(68,48),(87,24),(79,81),(21,81)],fill=1);d.rectangle((21,86,79,93),fill=1);d.ellipse((42,55,58,70),fill=0)
 elif kind=='urn':
  d.rectangle((34,8,66,17),fill=1);d.rectangle((39,17,61,29),fill=1);d.ellipse((20,24,80,84),fill=1);d.rectangle((34,80,66,92),fill=1);d.rectangle((37,42,63,52),fill=0)
 elif kind=='lantern':
  d.ellipse((36,4,64,27),fill=1);d.ellipse((43,10,57,21),fill=0);d.polygon([(31,24),(69,24),(82,36),(18,36)],fill=1);d.rectangle((25,39,75,80),fill=1);d.rectangle((36,45,64,72),fill=0);d.rectangle((18,83,82,94),fill=1)
 elif kind=='temple':
  d.polygon([(9,29),(50,6),(91,29)],fill=1);d.rectangle((12,32,88,40),fill=1)
  for x in [19,44,69]:d.rectangle((x,44,x+12,80),fill=1)
  d.rectangle((9,84,91,90),fill=1);d.rectangle((4,94,96,99),fill=1)
 elif kind=='scarab':
  d.ellipse((36,12,64,29),fill=1);d.ellipse((28,29,72,84),fill=1);d.rectangle((48,32,52,76),fill=0)
  for y in [34,51,68]:d.polygon([(30,y),(8,y-13),(8,y+2),(32,y+14)],fill=1);d.polygon([(70,y),(92,y-13),(92,y+2),(68,y+14)],fill=1)
 elif kind=='chalice':
  d.polygon([(15,9),(85,9),(75,45),(57,60),(57,78),(78,88),(78,95),(22,95),(22,88),(43,78),(43,60),(25,45)],fill=1);d.rectangle((29,17,71,25),fill=0)
 elif kind=='compass':
  d.ellipse((7,7,93,93),fill=1);d.ellipse((17,17,83,83),fill=0);d.polygon([(50,16),(61,41),(84,50),(59,60),(50,84),(40,60),(16,50),(41,40)],fill=1)
 elif kind=='feather':
  d.ellipse((30,7,78,70),fill=1);d.line((66,18,22,94),fill=1,width=7)
  for y in [26,40,54]:d.line((31,y,45,y+12),fill=0,width=5)
 im=im.resize((n,n),Image.Resampling.NEAREST);return [int(v) for v in im.getdata()]

rng=random.Random(146);out=[]
for index,kind in enumerate(['key','hourglass','crown','lantern','owl','ship','urn','scarab','temple','compass','chalice','feather']):
 n=[10,15,20][index//4];base=picture(kind,n);s=base[:]
 if not logic(s,n):
  found=False
  for changes in range(1,18):
   for attempt in range(100):
    s=base[:]
    # Fix ambiguous boundary cells without disturbing most of the silhouette.
    boundary=[i for i in range(n*n) if any(s[i]!=s[j] for j in [i-1,i+1,i-n,i+n] if 0<=j<n*n)]
    for i in rng.sample(boundary,min(changes,len(boundary))):s[i]=1-s[i]
    if logic(s,n):found=True;break
   if found:break
  if not found:raise Exception(kind)
 assert logic(s,n)
 out.append(dict(key=kind,name={'owl':'The Hidden Owl','key':'The Antique Key','ship':'The Lost Galleon'}.get(kind,'The '+kind.title()),size=n,mask=''.join(map(str,s))))
 print(kind,n,'adjusted',sum(a!=b for a,b in zip(base,s)),flush=True)
# Data only. Every puzzle is completely solved by line propagation, establishing uniqueness.
p=Path('app/src/main/java/com/ramy/mindgame/games/MosaicCatalog.kt')
p.write_text('package com.ramy.mindgame.games\ndata class MosaicPicture(val key: String,val name: String,val size: Int,val mask: String)\nobject MosaicCatalog { val all=listOf(\n'+',\n'.join('MosaicPicture('+json.dumps(x['key'])+','+json.dumps(x['name'])+','+str(x['size'])+','+json.dumps(x['mask'])+')' for x in out)+'\n) }\n')
Path('tools/mosaics-validation.json').write_text(json.dumps(out,indent=2))
