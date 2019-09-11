package com.bocse.perfume.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bocse on 05.03.2016.
 */
public class NoteTypeTranslatorRo {
    public NoteTypeTranslatorRo() {

    }

    public Map<NoteType, String> getLabels() {
        Map<NoteType, String> map = new HashMap<>();

        map.put(NoteType.ALDEHYDE, "Aldehidic");
        map.put(NoteType.POWDERY, "Pudrat");
        map.put(NoteType.ANIMALIC, "Animalic");
        map.put(NoteType.MUSK, "Mosc");
        map.put(NoteType.AQUATIC, "Acvatic");
        map.put(NoteType.HERBACIOUS, "Ierbos");
        map.put(NoteType.BEVERAGES, "Bautură");
        map.put(NoteType.CITRIC, "Citric");
        map.put(NoteType.EARTHY, "Pământiu");
        map.put(NoteType.GRAIN, "Cereal");
        map.put(NoteType.FLORAL, "Floral");
        map.put(NoteType.FRUITY, "Fructat");
        map.put(NoteType.GOURMANDY, "Gurmand");
        map.put(NoteType.MOSSY, "Mușchi");
        map.put(NoteType.GREEN, "Verde");
        map.put(NoteType.RESINOUS, "Rășinos");
        map.put(NoteType.LEATHER, "Piele");
        map.put(NoteType.TEXTILE, "Textil");
        map.put(NoteType.SYNTHETIC, "Sintetic");
        map.put(NoteType.AMBER, "Ambrat");
        map.put(NoteType.ORIENTAL, "Oriental");
        map.put(NoteType.BALSAMIC, "Balsamic");
        map.put(NoteType.MINERAL, "Mineral");
        map.put(NoteType.SPICY, "Picant");
        map.put(NoteType.TOBBACO, "Tutun");
        map.put(NoteType.WOODY, "Lemnos");
        map.put(NoteType.SMOKY, "Afumat");
        map.put(NoteType.TEA, "Ceai");
        map.put(NoteType.ALCOHOLIC_DISTILLED, "Alcoolic-Distilat");
        map.put(NoteType.ALCOHOLIC_FERMENTED, "Alcoolic-Fermentat");
        map.put(NoteType.NON_CLASSIFIED, "Necunoscut");
        map.put(NoteType.UNKNOWN, "Necunoscut");

        return map;
    }

    public Map<NoteType, Map<String, String>> loadDescriptionsFromFile(String file) throws IOException {
        Map<NoteType, Map<String, String>> map = new HashMap<>();
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                NoteType noteType = NoteType.valueOf(parts[0]);
                Map<String, String> node = new HashMap<>();
                node.put("label", parts[1]);
                node.put("description", parts[2]);
                map.put(noteType, node);
            }
        } catch (Exception ex) {
            System.out.println(line);
            throw ex;
        }
        return map;
    }
}
