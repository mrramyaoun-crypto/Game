import random,json
from pathlib import Path
R=random.Random(1442)
units=[[r*9+c for c in range(9)]for r in range(9)]+[[r*9+c for r in range(9)]for c in range(9)]+[[(br*3+r)*9+bc*3+c for r in range(3)for c in range(3)]for br in range(3)for bc in range(3)]
peers=[set().union(*(set(u)for u in units if i in u))-{i}for i in range(81)]
def count(board,limit=2):
 b=board[:];rows=[0]*9;cols=[0]*9;boxes=[0]*9
 for i,v in enumerate(b):
  if v:bit=1<<v;rows[i//9]|=bit;cols[i%9]|=bit;boxes[i//27*3+i%9//3]|=bit
 total=0
 def solve():
  nonlocal total
  if total>=limit:return
  best=-1;mask=0;num=10
  for i,v in enumerate(b):
   if not v:
    opts=1022&~(rows[i//9]|cols[i%9]|boxes[i//27*3+i%9//3]);k=opts.bit_count()
    if not k:return
    if k<num:best=i;mask=opts;num=k
    if k==1:break
  if best<0:total+=1;return
  r=best//9;c=best%9;box=best//27*3+best%9//3
  while mask:
   bit=mask&-mask;mask-=bit;b[best]=bit.bit_length()-1;rows[r]|=bit;cols[c]|=bit;boxes[box]|=bit;solve();rows[r]^=bit;cols[c]^=bit;boxes[box]^=bit;b[best]=0
   if total>=limit:break
 solve();return total

def rate(board):
 b=board[:];cand=[set(range(1,10))-{b[j]for j in peers[i]} if not b[i] else set()for i in range(81)];level=0
 def assign(i,v):
  b[i]=v;cand[i]=set()
  for j in peers[i]:cand[j].discard(v)
 for step in range(1000):
  if all(b):return level
  changed=False
  for i in range(81):
   if not b[i] and len(cand[i])==1:assign(i,next(iter(cand[i])));changed=True
  for u in units:
   for v in range(1,10):
    ids=[i for i in u if v in cand[i]]
    if len(ids)==1:assign(ids[0],v);changed=True
  if changed:continue
  for u in units:
   for v in range(1,10):
    ids=[i for i in u if v in cand[i]]
    if not ids:continue
    for other in units:
     if other==u:continue
     if all(i in other for i in ids):
      for i in set(other)-set(u):
       if v in cand[i]:cand[i].remove(v);changed=True
  if changed:level=max(level,1);continue
  for u in units:
   for i in u:
    if len(cand[i])!=2:continue
    matching=[j for j in u if cand[j]==cand[i]]
    if len(matching)==2:
     for j in set(u)-set(matching):
      old=len(cand[j]);cand[j]-=cand[i]
      if len(cand[j])!=old:changed=True
  if changed:level=max(level,2);continue
  return 3
 return 3

pools=[[]for _ in range(4)];seen=set();seed=0
while min(map(len,pools))<20:
 seed+=1;digits=R.sample(list(range(1,10)),9);rows=[b*3+r for b in R.sample([0,1,2],3)for r in R.sample([0,1,2],3)];cols=[b*3+c for b in R.sample([0,1,2],3)for c in R.sample([0,1,2],3)]
 sol=[digits[(rows[i//9]*3+rows[i//9]//3+cols[i%9])%9]for i in range(81)];b=sol[:];used=set()
 for i in R.sample(list(range(81)),81):
  v=b[i];b[i]=0
  if count(b)!=1:b[i]=v;continue
  clues=sum(v!=0 for v in b)
  if clues>43:continue
  level=rate(b);key=''.join(map(str,b))
  if level not in used and len(pools[level])<20 and (level>0 or clues<=40) and key not in seen:
   pools[level].append((key,''.join(map(str,sol))));used.add(level);seen.add(key)
 if seed%10==0:print(seed,list(map(len,pools)),flush=True)
 if seed>5000:raise Exception(list(map(len,pools)))
print('DONE',seed,list(map(len,pools)),flush=True)
names=['Easy','Medium','Hard','Expert']
Path('app/src/main/java/com/ramy/mindgame/games/SudokuCatalog.kt').write_text('package com.ramy.mindgame.games\nobject SudokuCatalog { val puzzles=mapOf(\n'+',\n'.join(json.dumps(name)+' to listOf('+','.join('"'+b+'" to "'+s+'"'for b,s in pool)+')'for name,pool in zip(names,pools))+'\n) }\n')
Path('tools/sudoku-validation.json').write_text(json.dumps(dict(zip(names,pools)),indent=2))
