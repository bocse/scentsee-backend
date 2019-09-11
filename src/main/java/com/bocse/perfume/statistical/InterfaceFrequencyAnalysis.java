package com.bocse.perfume.statistical;

import com.bocse.perfume.data.NoteType;

/**
 * Created by bogdan.bocse on 3/24/2016.
 */
public interface InterfaceFrequencyAnalysis {
    void process();

    Double getTopNoteRarity(String note);

    Double getHeartNoteRarity(String note);

    Double getBaseNoteRarity(String note);

    Double getTopNoteFrequency(String note);

    Double getHeartNoteFrequency(String note);

    Double getBaseNoteFrequency(String note);

    Double getMixedNoteFrequency(String note);

    Double getTopNoteTypeFrequency(NoteType noteType);

    Double getHeartNoteTypeFrequency(NoteType noteType);

    Double getBaseNoteTypeFrequency(NoteType noteType);

    Double getMixedNoteTypeFrequency(NoteType noteType);

    Double getMixedNoteRarity(String note);

    Double getTopNoteTypeRarity(NoteType noteType);

    Double getHeartNoteTypeRarity(NoteType noteType);

    Double getBaseNoteTypeRarity(NoteType noteType);

    Double getMixedNoteTypeRarity(NoteType noteType);
}
