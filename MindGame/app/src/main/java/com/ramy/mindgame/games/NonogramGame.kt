package com.ramy.mindgame.games
import com.ramy.mindgame.core.*
import org.json.*
import kotlin.random.Random
import kotlin.math.sqrt

class NonogramGame(random: Random=Random.Default,size: Int=10,pictureKey: String?=null): Game {
    override val id="nonogram";override var moves=0;override var won=false;override var lost=false
    var pictureKey=(if(pictureKey!=null)pictures.first{it.key==pictureKey} else pictures.filter{it.size==size}.random(random)).key
    var size=pictures.first{it.key==this.pictureKey}.size
    var solution=pictures.first{it.key==this.pictureKey}.mask.map{it-'0'}.toIntArray();var board=IntArray(this.size*this.size)
    private val history=ArrayDeque<IntArray>()
    val title get()=pictures.firstOrNull{it.key==pictureKey}?.name ?: "Legacy mosaic"
    fun row(r: Int)=runs(IntArray(size){solution[r*size+it]})
    fun col(c: Int)=runs(IntArray(size){solution[it*size+c]})
    fun rowDone(r: Int)=runs(IntArray(size){if(board[r*size+it]==1)1 else 0})==row(r)
    fun colDone(c: Int)=runs(IntArray(size){if(board[it*size+c]==1)1 else 0})==col(c)
    fun beginStroke()=board.copyOf()
    fun paint(i: Int,value: Int) {if(!won&&i in board.indices)board[i]=value}
    fun endStroke(before: IntArray) {if(!before.contentEquals(board)){history.addLast(before);if(history.size>40)history.removeFirst();moves++;check()}}
    private fun check() {won=board.indices.all{(board[it]==1)==(solution[it]==1)}}
    fun mark(i: Int,cross: Boolean=false) {if(!won){val before=beginStroke();val v=if(cross)2 else 1;paint(i,if(board[i]==v)0 else v);endStroke(before)}}
    fun undo(): Boolean {if(history.isEmpty())return false;board=history.removeLast();moves++;won=false;return true}
    override val score get()=if(won) maxOf(100,size*size*10-moves*2) else 0
    override fun save()=base().put("size",size).put("pictureKey",pictureKey).put("solution",solution.json()).put("board",board.json()).put("undo",JSONArray(history.map{it.json()}))
    companion object {
        val pictures=MosaicCatalog.all
        fun runs(a: IntArray): List<Int> {val out=mutableListOf<Int>();var n=0;for(v in a){if(v==1)n++ else if(n>0){out.add(n);n=0}};if(n>0)out.add(n);return out.ifEmpty{listOf(0)}}
        fun patterns(n: Int,clues: List<Int>): List<Int> {if(clues==listOf(0))return listOf(0);val out=mutableListOf<Int>();fun place(k: Int,start: Int,mask: Int){if(k==clues.size){out.add(mask);return};val tail=clues.drop(k+1).sum()+clues.size-k-1;for(x in start..n-clues[k]-tail)place(k+1,x+clues[k]+1,mask or (((1 shl clues[k])-1) shl x))};place(0,0,0);return out}
        /** Complete line-constraint deduction also proves each bundled picture has one answer. */
        fun unique(s: IntArray): Boolean {
            val n=sqrt(s.size.toDouble()).toInt();val b=IntArray(s.size){-1};val lines=(0 until n*2).map{l->patterns(n,runs(IntArray(n){k->s[if(l<n)l*n+k else k*n+l-n]}))}.toMutableList()
            repeat(n*n){var changed=false;for(l in lines.indices){val ids=IntArray(n){k->if(l<n)l*n+k else k*n+l-n};var ones=0;var zeros=0;ids.forEachIndexed{k,i->if(b[i]==1)ones=ones or (1 shl k) else if(b[i]==0)zeros=zeros or (1 shl k)};val opts=lines[l].filter{it and ones==ones&&it and zeros==0};if(opts.isEmpty())return false;lines[l]=opts;var all=(1 shl n)-1;var any=0;opts.forEach{all=all and it;any=any or it};ids.forEachIndexed{k,i->val v=if(all shr k and 1==1)1 else if(any shr k and 1==0)0 else -1;if(v>=0&&b[i]<0){b[i]=v;changed=true}}};if(!changed)return b.contentEquals(s)};return b.contentEquals(s)
        }
        fun load(j: JSONObject)=NonogramGame(pictureKey=j.optString("pictureKey","key").let{key->if(pictures.any{it.key==key})key else "key"}).apply{restoreBase(j);solution=j.ints("solution");board=j.ints("board");size=j.optInt("size",sqrt(board.size.toDouble()).toInt());pictureKey=j.optString("pictureKey","legacy");j.optJSONArray("undo")?.let{a->repeat(a.length()){k->history.addLast(IntArray(board.size){a.getJSONArray(k).getInt(it)})}}}
    }
}
