package com.bocse.perfume;

import com.bocse.perfume.data.*;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.serializer.JsonSerializer;
import com.bocse.perfume.signature.SignatureEvaluator;
import com.bocse.perfume.utils.TextUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 12/2/2015.
 */
public class NoteExtractorMain {
    public final static FileConfiguration configuration = new PropertiesConfiguration();
    private final static Logger logger = Logger.getLogger(PerfumeMain.class.toString());

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        configuration.load(args[0]);
        SignatureEvaluator signatureEvaluator = new SignatureEvaluator();
        Map<String, Set<NoteType>> noteTypeMap = signatureEvaluator.getAllNoteClasses();
        JsonSerializer.serialize(configuration.getString("output.notes.path") + "allNotes.json", noteTypeMap);

        String perfumeListFilename = configuration.getString("output.perfumesCompact.path");
        PerfumeIterator iterator = new PerfumeIterator();
        iterator.iterateAndKeep(new File(perfumeListFilename));
        iterator.swap();
        Set<String> strictlyMatchedNotes = new HashSet<>();
        Set<String> partiallyMatchedNotes = new HashSet<>();
        Set<String> missingNotes = new HashSet<>();
        for (Perfume perfume : iterator.getPerfumeList()) {
            List<String> allNotes = new ArrayList<>();
            allNotes.addAll(perfume.getTopNotes());
            allNotes.addAll(perfume.getHeartNotes());
            allNotes.addAll(perfume.getBaseNotes());

            for (String noteIterator : allNotes) {
                String note = TextUtils.flattenToAscii(noteIterator.toLowerCase());
                if (!noteTypeMap.containsKey(note)) {
                    Boolean found = false;
                    for (Map.Entry<String, Set<NoteType>> candidate : noteTypeMap.entrySet()) {
                        if (note.contains(candidate.getKey())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        partiallyMatchedNotes.add(noteIterator);
                    } else {
                        missingNotes.add(noteIterator);
                    }
                } else {
                    strictlyMatchedNotes.add(noteIterator);
                }
            }

        }
        JsonSerializer.serialize(configuration.getString("output.notes.path") + "partiallyMatched.json", partiallyMatchedNotes);
        JsonSerializer.serialize(configuration.getString("output.notes.path") + "missingNotes.json", missingNotes);

        logger.info("Strictly matched notes: \t" + strictlyMatchedNotes.size());
        logger.info("Partially matched notes: \t" + partiallyMatchedNotes.size());
        logger.info("Missing notes: \t" + missingNotes.size());
    }


}
