package testSite;


import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class SyllableTest {
    private final static Logger logger = Logger.getLogger(SyllableTest.class.toString());


    public static void main(String[] args) throws IOException {
        PerfumeIterator perfumeIterator = new PerfumeIterator();
        perfumeIterator.iterateAndKeep(new File("/Users/bogdan.bocse/PetProjects/data/perfumes/compact.json"));
        perfumeIterator.swap();
        Map<String, AtomicInteger> frequentLastSyllable = new HashMap<>();
        for (Perfume perfume : perfumeIterator.getPerfumeList()) {
            if (perfume.isSubstandard())
                continue;
            if (!perfume.getGender().equals(Gender.UNI))
                continue;
            String[] nameParts = perfume.getName().split(" ");
            String part = nameParts[0];
            //for (String part:nameParts)
            {
                List<String> syllableList = TextUtils.syllableSplit(part, 5);
                if (syllableList.isEmpty())
                    continue;
                String lastSyllable = syllableList.get(syllableList.size() - 1);
                frequentLastSyllable.putIfAbsent(lastSyllable, new AtomicInteger(0));
                frequentLastSyllable.get(lastSyllable).incrementAndGet();
            }
        }
        for (Map.Entry<String, AtomicInteger> entry : frequentLastSyllable.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
        /*
        SignatureEvaluator signatureEvaluator = new SignatureEvaluator();
        signatureEvaluator.iterateAndKeep(new File("/Users/bogdan.bocse/PetProjects/data/notes/allNotes.json"));
        LinkedHashSet<String> syllableSet=new LinkedHashSet<>();
        for (String note: signatureEvaluator.getNoteTypeMap().keySet()) {
            syllableSet.addAll(TextUtils.syllableSplit(note));
        }
        List<String> syllableList=new ArrayList<>();
        syllableList.addAll(syllableSet);

        logger.info(syllableSet.toString());
        Random random=new Random(784239423423L);
        for (int i=0; i<100; i++) {
            int count = 2 + random.nextInt(3);
            StringBuffer sb=new StringBuffer();
            for (int j = 0; j < count; j++)
            {
                int syllableIndex=random.nextInt(syllableList.size());
                sb.append(syllableList.get(syllableIndex));
                if (random.nextDouble()<0.3)
                    sb.append(" ");
            }
            System.out.println(StringUtils.capitalize(sb.toString()));
        }
        */
    }
}
