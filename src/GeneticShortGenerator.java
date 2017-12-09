import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * For sign purposes, this class uses int as if it was an unsigned short in order to generate
 * the desired numbers. The numbers generated will always be 16bits size as a maximum though.
 */
public class GeneticShortGenerator {

    private Random r;

    private static final String[] choice = {"0", "1"};

    private static final float GRADED_RETAIN_PERCENT = 0.3f;
    private static final float NONGRADED_RETAIN_PERCENT = 0.2f;

    private int amount;
    private int set;

    /**
     * Creates a new short array generator
     * @param amount Amount of shorts to be generated for each generation
     * @param set How many bits should be set for each short
     */
    public GeneticShortGenerator(int amount, int set){
        r = new Random();
        this.amount = amount;
        this.set = set;
    }

    /**
     * @return A Stringified bit picked randomly
     */
    private String getBit(){
        return choice[r.nextInt(2)];
    }

    /**
     * @return A new chromosome for the shor generation
     */
    private String createChromosome(){
        StringBuilder ch = new StringBuilder("");
        for(int i = 0; i < 16; i++)
            ch.append(getBit());

        return ch.toString();
    }

    /**
     * @param chromosome Chromosome to be evaluated
     * @return Score for this chromosome
     */
    private float getScore(String chromosome){
        float setCount = 0;

        for(int i = 0; i < 16; i++){
            if(chromosome.charAt(i) == '1')
                setCount++;
        }

        return setCount <= set? setCount: set-setCount;
    }

    /**
     * Simulates natural selection between the current living chromosomes
     * @param chromosomes Current living chromosomes
     * @return Those who live to reproduce
     */
    private String[] naturalSelection(String[] chromosomes){

        ArrayList<String> selected = new ArrayList<>();

        int popln = chromosomes.length;
        int graded_popln = (int) (popln * GRADED_RETAIN_PERCENT);
        int ngraded_popln = (int) (popln * NONGRADED_RETAIN_PERCENT);

        ArrayList<String> chs = new ArrayList<>(Arrays.asList(chromosomes));
        chs.sort((o1, o2) -> {
            float score1 = getScore(o1),
                    score2 = getScore(o2);

            return Float.compare(score1, score2);
        });

        Collections.reverse(chs);

        for(int i = 0; i < graded_popln; i++){
            selected.add(chs.get(i));
        }

        for(int i = 0; i < ngraded_popln; i++){
            int pos = ThreadLocalRandom.current().nextInt(graded_popln, chs.size());
            selected.add(chs.get(pos));
            chs.remove(pos);
        }

        return selected.toArray(new String[]{});
    }

    /**
     * Simulates genetic recombination of both parents into a child
     * @param parent1 Parent chromosome 1
     * @param parent2 Parent chromosome 2
     * @return
     */
    private String crossover(String parent1, String parent2){

        int half = 16/2;
        return parent1.substring(0, half)+parent2.substring(half);
    }

    /**
     * Simulates genetic mutation after genetic recombination
     * @param chr Chromosome to be mutated
     * @return mutated chromosome
     */
    private String mutation(String chr){
        int pos = ThreadLocalRandom.current().nextInt()%16;
        if(pos < 0) pos= -pos;
        char old = chr.charAt(pos);

        //Mutation = Bit Toggle
        String newb = old== '1'? "0":"1";
        return chr.substring(0, pos)+newb+chr.substring(pos+1);
    }

    /**
     * Creates the first generation of chromosomes
     * @param popSize Amount of members for the first generation
     * @return First generation of chromosomes
     */
    private String[] createPopulation(int popSize){
        String[] pop = new String[popSize];
        for(int i = 0; i < popSize; i++){
            pop[i] = createChromosome();
        }

        return pop;
    }

    /**
     * Generates the new generation of individuals after the current ones
     * @param popln Current individuals
     * @return new generation
     */
    private String[] newGeneration(String[] popln){

        String[] selected = naturalSelection(popln);

        ArrayList<String> children = new ArrayList<>();

        while(children.size() < popln.length-selected.length){

            String parent1 = selected[ThreadLocalRandom.current().nextInt(selected.length)];
            String parent2 = selected[ThreadLocalRandom.current().nextInt(selected.length)];

            String child = crossover(parent1, parent2);

            int mutationProbability = ThreadLocalRandom.current().nextInt(100);
            if(mutationProbability <= 50){
                child = mutation(child);
            }
            children.add(child);
        }

        children.addAll(Arrays.asList(selected));
        return children.toArray(new String[]{});
    }

    /**
     * @return wether the individual chr is a fit partial answer
     */
    private boolean isAnswer(String chr){
        int set = 0;
        for (int i = 0; i < 16; i++){
            if(chr.charAt(i)=='1')
                set++;
        }

        return set > 0 && set <= this.set;
    }

    /**
     * Generates a short array of the size specified
     * @return Generation result
     */
    public int[] generate(){
        String[] popl = createPopulation(200);
        ArrayList<String> ans = new ArrayList<>();
        int ansPop = 0;

        while(ansPop < amount){
            popl = newGeneration(popl);

            for (String individual:
                 popl) {
                if(isAnswer(individual)){
                    ans.add(individual);
                    ansPop++;
                }
            }
        }

        int[] answers = new int[8];
        for (int i = 0; i < 8; i++){
            answers[i] = Integer.parseInt(ans.get(i), 2);
        }

        return answers;
    }

    public static void main(String[] args) {
        int[] lvl;


        GeneticShortGenerator gen = new GeneticShortGenerator(8, 1);
        lvl = gen.generate();

        for (int ind:
             lvl) {
            System.out.print(String.format("0x%1$04X ", ind));
        }
    }

}
