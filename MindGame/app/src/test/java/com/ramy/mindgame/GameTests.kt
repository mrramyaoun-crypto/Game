package com.ramy.mindgame
import com.ramy.mindgame.games.*
import com.ramy.mindgame.core.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameTests {
    @Test fun repeatedCodeDigitsCountOnce() {
        assertEquals(Feedback(2,0),feedback(intArrayOf(1,1,2,3),intArrayOf(1,2,2,2)))
        assertEquals(Feedback(0,4),feedback(intArrayOf(1,1,2,2),intArrayOf(2,2,1,1)))
        assertEquals(Feedback(4,0),feedback(intArrayOf(0,0,0,0),intArrayOf(0,0,0,0)))
    }
    @Test fun enigmaCluesHaveExactlyOneAnswer() {
        repeat(6) { seed -> val g=CodeGame("enigma",Random(seed)); val matches=(0..9999).count { n -> val candidate=intArrayOf(n/1000,n/100%10,n/10%10,n%10); g.clues.all { feedback(candidate,it)==feedback(g.secret,it) } }; assertEquals(1,matches) }
    }
    @Test fun sudokuUniqueAndCompletable() {
        repeat(4) { seed -> val g=SudokuGame(Random(seed)); assertEquals(1,SudokuGame.countSolutions(g.board)); for(i in g.board.indices) if(g.fixed[i]==0) g.enter(i,g.solution[i]); assertTrue(g.won); assertFalse(g.board.indices.any { g.conflicts(it,g.board[it]) }) }
    }
    @Test fun sudokuRejectsChangesToGivenCells() { val g=SudokuGame(Random(4)); val i=g.fixed.indexOf(1); val v=g.board[i]; g.enter(i,0); assertEquals(v,g.board[i]); assertEquals(0,g.moves) }
    @Test fun minesFirstTapSafeEverywhere() {
        repeat(64) { i -> val g=MinesGame(Random(i)); g.reveal(i); assertFalse(g.lost); assertEquals(0,g.mines[i]); assertTrue(g.neighbors(i).all { g.mines[it]==0 }); assertEquals(10,g.mines.sum()) }
    }
    @Test fun minesFlagsBlockRevealAndWinningNeedsAllSafeCells() { val g=MinesGame(Random(1)); g.flag(0); g.reveal(0); assertFalse(g.planted); g.flag(0); g.reveal(0); for(i in g.mines.indices) if(g.mines[i]==0) g.reveal(i); assertTrue(g.won) }
    @Test fun minesLossDoesNotMutateAfterEnd() { val g=MinesGame(Random(2)); g.reveal(0); g.reveal(g.mines.indexOf(1)); assertTrue(g.lost); val before=g.save().toString(); g.reveal(63); g.flag(62); assertEquals(before,g.save().toString()) }
    @Test fun tilesMergeOnce() { val g=TilesGame(Random(1)); g.board=IntArray(16); for(i in 0..3) g.board[i]=2; assertTrue(g.slide(0)); assertEquals(4,g.board[0]); assertEquals(4,g.board[1]); assertEquals(8,g.score); assertEquals(1,g.moves) }
    @Test fun tilesNoOpDoesNotSpawn() { val g=TilesGame(Random(1)); g.board=IntArray(16); g.board[0]=2; assertFalse(g.slide(0)); assertEquals(1,g.board.count { it!=0 }); assertEquals(0,g.moves) }
    @Test fun tilesAllDirectionsAndDeadBoard() {
        repeat(4) { d -> val g=TilesGame(Random(1)); g.board=IntArray(16); g.board[5]=2; g.board[6]=2; assertTrue(g.slide(d)); if(d==0 || d==2) assertEquals(4,g.score) else assertEquals(0,g.score) }
        val g=TilesGame(); g.board=IntArray(16) { if((it/4+it%4)%2==0) 2 else 4 }; assertFalse(g.canMove())
    }
    @Test fun memoryPairsAndMismatchRecovery() {
        val g=MemoryGame(Random(3)); assertTrue((0..7).all { v -> g.cards.count { it==v }==2 }); g.flip(0); g.flip(g.cards.indices.first { g.cards[it]!=g.cards[0] }); assertTrue(g.second>=0); g.hide(); assertEquals(-1,g.first)
        for(v in 0..7) { val pair=g.cards.indices.filter { g.cards[it]==v }; g.flip(pair[0]); g.flip(pair[1]) }; assertTrue(g.won); assertEquals(16,g.matched.sum())
    }
    @Test fun nonogramUniqueAndSolved() { repeat(16) { seed -> val g=NonogramGame(Random(seed)); assertTrue(NonogramGame.unique(g.solution)); for(i in g.solution.indices) if(g.solution[i]==1) g.mark(i); assertTrue(g.won) } }
    @Test fun saveRoundTripAllGames() {
        Registry.games.forEach { info -> val g=info.create(); when(g) { is CodeGame -> g.submit(intArrayOf(1,2,3,4)); is SudokuGame -> g.enter(g.fixed.indexOf(0),1); is MinesGame -> { g.reveal(0); g.flag(63) }; is MemoryGame -> g.flip(0); is TilesGame -> g.slide(0); is NonogramGame -> g.mark(0) }; val j=g.save(); val restored=info.load(j); assertEquals(info.id,restored.id); assertEquals(j.toString(),restored.save().toString()) }
    }
    @Test fun minesDifficultyPresetsAndSaveRestore() {
        for(preset in listOf(Triple(8,8,10),Triple(16,16,40),Triple(30,16,99))) {
            val g=MinesGame(Random(12),preset.first,preset.second,preset.third);g.reveal(preset.first+1)
            assertEquals(preset.third,g.mines.sum());assertFalse(g.lost);assertEquals(preset.first*preset.second,g.mines.size)
            val restored=MinesGame.load(g.save());assertEquals(preset.first,restored.columns);assertEquals(preset.second,restored.rows);assertEquals(preset.third,restored.mineCount);assertArrayEquals(g.mines,restored.mines)
            assertFalse(g.neighbors(preset.first-1).contains(preset.first))
        }
    }
    @Test fun sudokuPencilNotesToggleAndPersist() {
        val g=SudokuGame(Random(1));val i=g.fixed.indexOf(0);g.note(i,3);assertEquals(1 shl 3,g.notes[i]);val restored=SudokuGame.load(g.save());assertEquals(g.notes[i],restored.notes[i]);g.note(i,3);assertEquals(0,g.notes[i]);g.note(i,4);g.enter(i,g.solution[i]);assertEquals(0,g.notes[i])
    }
}
