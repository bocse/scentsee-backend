package com.bocse.perfume.signature;

import com.bocse.perfume.data.FragranticaNoteType;
import com.bocse.perfume.data.Note;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.utils.TextUtils;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.*;

/**
 * Created by bogdan.bocse on 12/2/2015.
 */
@Deprecated
public class FragranticaSignatureEvaluator {
    private Map<String, Note> noteTypeMap;
    private SortedMap<String, MutableInt> unknownAccumulatorCounter = new TreeMap<>();
    private Map<String, Note> unknownAccumulator = new HashMap<>();
    private int matchExactCount = 0;
    private int matchExactAscii = 0;
    private int matchContains = 0;
    private int matchOverlap = 0;
    private int matchContainsTypeName = 0;

    public FragranticaSignatureEvaluator(Map<String, Note> noteTypeMap) {
        this.noteTypeMap = noteTypeMap;
    }

    public Map<String, Note> getNoteTypeMap() {
        return noteTypeMap;
    }

    public SortedMap<String, MutableInt> getUnknownAccumulatorCounter() {
        return unknownAccumulatorCounter;
    }

    public Map<String, Note> getUnknownAccumulator() {
        return unknownAccumulator;
    }

    private Map<FragranticaNoteType, Integer> getSignature(List<String> notesCollection) {
        Map<FragranticaNoteType, Integer> signature = new TreeMap<>();
        for (String noteIterator : notesCollection) {
            String note = noteIterator.replaceAll("notes", "").replaceAll("accord", "").trim();
            //String note=noteIterator;
            FragranticaNoteType fragranticaNoteType = FragranticaNoteType.UNKNOWN;
            if (noteTypeMap.containsKey(note)) {
                matchExactCount++;
                fragranticaNoteType = noteTypeMap.get(note).getFragranticaNoteType();
            } else {

                for (Note noteValue : noteTypeMap.values()) {
                    if (TextUtils.flattenToAscii(noteValue.getName()).equals(TextUtils.flattenToAscii(note))
                            || note.contains(noteValue.getName())) {
                        fragranticaNoteType = noteValue.getFragranticaNoteType();
                        matchExactAscii++;
                        break;
                    }


                }

                if (fragranticaNoteType.equals(FragranticaNoteType.UNKNOWN))
                    for (Note noteValue : noteTypeMap.values()) {
                        if (
                                TextUtils.flattenToAscii(note).contains(TextUtils.flattenToAscii(noteValue.getName()))
                                        || note.contains(noteValue.getName())) {
                            fragranticaNoteType = noteValue.getFragranticaNoteType();
                            matchContains++;
                            break;
                        }
                    }

                if (fragranticaNoteType.equals(FragranticaNoteType.UNKNOWN)) {
                    int maxMatch = 0;
                    Note matchedNote = null;
                    for (Note noteValue : noteTypeMap.values()) {
                        String[] noteValueParts = noteValue.getName().split("[ ()]");
                        String[] noteParts = note.split("[ ()]");
                        int noteMatch = 0;
                        for (int noteValueIndex = 0; noteValueIndex < noteValueParts.length; noteValueIndex++)
                            for (int noteIndex = 0; noteIndex < noteParts.length; noteIndex++)
                                if (noteValueParts[noteValueIndex].equals(noteParts[noteIndex])) {
                                    noteMatch++;
                                }
                        if (noteMatch > maxMatch) {
                            matchedNote = noteValue;
                            maxMatch = noteMatch;
                        }
                    }
                    if (matchedNote != null) {
                        fragranticaNoteType = matchedNote.getFragranticaNoteType();
                        matchOverlap++;
                    }

                }

                if (fragranticaNoteType.equals(FragranticaNoteType.UNKNOWN)) {
                    for (FragranticaNoteType fragranticaNoteTypeIterator : FragranticaNoteType.values())
                        if (note.contains(fragranticaNoteTypeIterator.toString().toLowerCase())) {
                            fragranticaNoteType = fragranticaNoteTypeIterator;
                            matchContainsTypeName++;
                            break;
                        }
                }
            }

            if (fragranticaNoteType.equals(FragranticaNoteType.UNKNOWN)) {
                if (!unknownAccumulatorCounter.containsKey(note)) {
                    unknownAccumulatorCounter.put(note, new MutableInt());
                }
                unknownAccumulatorCounter.get(note).add(1);
                Note unknownNote = new Note();
                unknownNote.setName(note);
                unknownNote.setFragranticaNoteType(fragranticaNoteType);

                unknownAccumulator.put(note, unknownNote);
            }


            if (signature.containsKey(fragranticaNoteType)) {
                signature.put(fragranticaNoteType, signature.get(fragranticaNoteType) + 1);
            } else {
                signature.put(fragranticaNoteType, 1);
            }
        }
        return signature;
    }

    public Map<FragranticaNoteType, Integer> getTopSignature(Perfume perfume) {
        return getSignature(perfume.getTopNotes());
    }

    public Map<FragranticaNoteType, Integer> getHeartSignature(Perfume perfume) {
        return getSignature(perfume.getHeartNotes());
    }

    public Map<FragranticaNoteType, Integer> getBaseSignature(Perfume perfume) {
        return getSignature(perfume.getBaseNotes());
    }

    public Map<FragranticaNoteType, Integer> getMixedSignature(Perfume perfume) {
        return getSignature(perfume.getMixedNotes());
    }


    public int getMatchExactCount() {
        return matchExactCount;
    }

    public int getMatchExactAscii() {
        return matchExactAscii;
    }

    public int getMatchContains() {
        return matchContains;
    }

    public int getMatchOverlap() {
        return matchOverlap;
    }

    public int getMatchContainsTypeName() {
        return matchContainsTypeName;
    }


}
