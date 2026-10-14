package com.codebreaker.game;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class OriginalEnigmaLogicTest {
    @Test(timeout=30000) public void recoveredGeneratorPreservesOriginalRulesAndUniqueAnswers() {
        MainActivity activity=Robolectric.buildActivity(MainActivity.class).get();
        activity.genCodes("",new boolean[10]);assertEquals(5040,activity.ALL.size());
        activity.rnd.setSeed(42);
        for(String mode:new String[]{"easy","normal","hard"}) {
            MainActivity.Puzzle puzzle=activity.generate(mode);
            assertEquals(5,puzzle.clues.size());assertEquals(4,puzzle.secret.chars().distinct().count());
            assertEquals(1,activity.filter(activity.ALL,puzzle.clues).size());
            assertEquals(puzzle.secret,activity.filter(activity.ALL,puzzle.clues).get(0));
        }
    }
}
