package com.bocse.perfume.statistical;

import com.bocse.perfume.data.NoteType;

/**
 * Created by bogdan.bocse on 3/24/2016.
 */
public class DummyFrequencyAnalysis implements InterfaceFrequencyAnalysis {

    @Override
    public void process() {

    }

    @Override
    public Double getTopNoteRarity(String note) {
        return 1.0;
    }

    @Override
    public Double getHeartNoteRarity(String note) {
        return 1.0;
    }

    @Override
    public Double getBaseNoteRarity(String note) {
        return 1.0;
    }

    @Override
    public Double getTopNoteFrequency(String note) {
        return 0.0;
    }

    @Override
    public Double getHeartNoteFrequency(String note) {
        return 0.0;
    }

    @Override
    public Double getBaseNoteFrequency(String note) {
        return 0.0;
    }

    @Override
    public Double getMixedNoteFrequency(String note) {
        return 0.0;
    }

    @Override
    public Double getTopNoteTypeFrequency(NoteType noteType) {
        return 0.0;
    }

    @Override
    public Double getHeartNoteTypeFrequency(NoteType noteType) {
        return 0.0;
    }

    @Override
    public Double getBaseNoteTypeFrequency(NoteType noteType) {
        return 0.0;
    }

    @Override
    public Double getMixedNoteTypeFrequency(NoteType noteType) {
        return 0.0;
    }

    @Override
    public Double getMixedNoteRarity(String note) {
        return 1.0;
    }

    @Override
    public Double getTopNoteTypeRarity(NoteType noteType) {
        return 1.0;
    }

    @Override
    public Double getHeartNoteTypeRarity(NoteType noteType) {
        return 1.0;
    }

    @Override
    public Double getBaseNoteTypeRarity(NoteType noteType) {
        return 1.0;
    }

    @Override
    public Double getMixedNoteTypeRarity(NoteType noteType) {
        return 1.0;
    }
}
