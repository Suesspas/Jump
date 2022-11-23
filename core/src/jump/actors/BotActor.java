package jump.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import jump.GameStage;
import jump.neuralNetwork.NeuralNetwork;

import java.util.Arrays;

public class BotActor extends HeroActor{
    private NeuralNetwork neuralNetwork;
    private int jumpTimer;

    private boolean isAlive;
    private float score;

    private boolean reachedGoal;
    private static final Vector2 spawn = new Vector2(8f,5f);



    public BotActor(int botNumber) {
        super(GameStage.WorldMisc.createHero(spawn, botNumber));
        neuralNetwork = new NeuralNetwork(6,5,3); //TODO figure out topology
        jumpTimer = 0;
        isAlive = true;
        reachedGoal = false;
        score = 0;
    }

    public BotActor(NeuralNetwork.FlattenNetwork net, int botNumber) {
        super(GameStage.WorldMisc.createHero(spawn, botNumber));
        neuralNetwork = NeuralNetwork.expand(net);
        jumpTimer = 0;
        isAlive = true;
        reachedGoal = false;
        score = 0;
    }

    public void update(GoalActor goal) {
        this.score = Math.max(this.score, scoreCalc(goal));
    }

    private float scoreCalc(GoalActor goal) {
        return (reachedGoal ? 1000 : 100) / (1 + distanceTo(goal.body.getPosition()));
    }

    public float distanceTo(Vector2 target){
        return target.dst(this.body.getPosition());
    }

    public float getScore() {
        return score;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public void feed(PlatformActor closestPlatform, float distanceToGoal) { //TODO sinvolle inputs
        if (closestPlatform != null && isAlive) {

            float[] inputs = {
                    1f, //
                    distanceToGoal , // alternative distance / GameStage.minWorldWidth
                    this.body.getPosition().x,
                    this.body.getPosition().y,
                    closestPlatform.getX(),
                    closestPlatform.getY(),
            };

            float[] output = this.getNeuralNetwork().eval(inputs); //TODO outputs in what range?
            if (this.getUserData().getBotNumber() == 1){
                System.out.println("Bot number " + this.getUserData().getBotNumber() + " outputs: " + Arrays.toString(output));
            }
            if (output[0] > 0.5 && output[0] > output[1]){
                this.moveRight();
            } else if (output[1] > 0.5 && output[1] > output[0]){
                this.moveLeft();
            } else {
                this.moveStop();
            }
            if (output[2]> 0.5)
                this.jump();

        }
    }

    @Override
    public void jump() {
        if (jumpTimer > 0) {
            return;
        }
        super.jump();
        jumpTimer = 15;
    }

    public void decrementJumpTimer(){
        jumpTimer--;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void dead() {
        isAlive = false;
    }

    @Override
    public void landed() {
        super.landed();
        jumpTimer = 2;
    }

    public void setNumber(int botNumber) { //TODO whack
        body = GameStage.WorldMisc.createHero(spawn, botNumber);
    }

    public boolean isOutOfBounds(float x, float y){
        return (this.body.getPosition().x > x) || (this.body.getPosition().x < 0)
                || (this.body.getPosition().y > y) || (this.body.getPosition().y < 0);
    }

    public void reachedGoal() {
        reachedGoal = true;
    }
}
