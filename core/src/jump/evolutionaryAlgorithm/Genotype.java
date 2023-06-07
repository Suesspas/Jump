package jump.evolutionaryAlgorithm;

import jump.actors.BotActor;
import jump.neuralNetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Genotype {

	private BotActor bot;

	private float fitness;

	public BotActor getBot() {
		return bot;
	}

	public float getFitness() {
		return fitness;
	}
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}


	public Genotype(BotActor bot) {
		this.fitness = 0;
		this.bot = bot;
	}

	public Genotype(NeuralNetwork.FlattenNetwork net, int botNumber, String scoreEvaluation) {
		//TODO
		this.bot = new BotActor(net, botNumber, scoreEvaluation);
		this.fitness = 0;
	}

	public Genotype(Genotype genome, int botNumber, String scoreEvaluation) {
		this(genome.getBot().getNeuralNetwork().flatten(), botNumber, scoreEvaluation);
	}

	public static List<Genotype> crossOver(Genotype geneA, Genotype geneB, int childCount, float mutationRate, float mutationRange,
										   float mutationStep, boolean isUniform, String scoreEvaluation, String crossoverType) {
		List<Genotype> children = new ArrayList<Genotype>();
		for (int ch = 0; ch < childCount; ch++) {
			NeuralNetwork.FlattenNetwork childNet = geneA.bot.getNeuralNetwork().flatten();
			NeuralNetwork.FlattenNetwork parentNet = geneB.bot.getNeuralNetwork().flatten();
			if (crossoverType.equals("std")){ //std ist discrete crossover mit 50/50 chance für gene einer der eltern
				for (int i = 0; i < childNet.weights.size(); i++) {
					if (Math.random() <= 0.5) {
						childNet.weights.set(i, parentNet.weights.get(i));
					}
				}
			} else if (crossoverType.equals("arith")){ //arithmetischer crossover mit avg value aller genes
				for (int i = 0; i < childNet.weights.size(); i++) {
					if (Math.random() <= 0.5) {
						childNet.weights.set(i, (parentNet.weights.get(i) + childNet.weights.get(i)) / 2);
					}
				}
			} else {
				throw new RuntimeException("invalid crossover type parameter");
			}

			if (isUniform){
				uniformMutation(mutationRate, mutationRange, childNet);
			} else {
				nonuniformMutation(mutationRate, mutationRange, mutationStep, childNet);
			}
			children.add(new Genotype(childNet, 0, scoreEvaluation)); //default bot number 0, is set after method call
		}
		return children;
	}

	private static void nonuniformMutation(float mutationRate, float mutationRange, float mutationStep, NeuralNetwork.FlattenNetwork childNet) {
		Random random = new Random();
		for (int i = 0; i < childNet.weights.size(); i++) {
			if (Math.random() <= mutationRate) {
				float currentValue = childNet.weights.get(i);
				float randomValue = (float) random.nextGaussian() * mutationStep;
				float newValue = currentValue + randomValue;
				newValue = Math.max(-mutationRange, Math.min(mutationRange, newValue));
				childNet.weights.set(i, newValue);
			}
		}
	}

	private static void uniformMutation(float mutationRate, float mutationRange, NeuralNetwork.FlattenNetwork childNet) {
		for (int i = 0; i < childNet.weights.size(); i++) {
			if (Math.random() <= mutationRate) {
				childNet.weights.set(i, (float) Math.random()*2* mutationRange - mutationRange); //TODO schauen, ob der Wertebereich sinvoll ist
			}
		}
	}

	public void assignBodyNumber(int botNumber) {
		this.bot.assignBodyNumber(botNumber);
	}
}
