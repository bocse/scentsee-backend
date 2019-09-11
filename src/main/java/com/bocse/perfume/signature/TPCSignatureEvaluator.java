package com.bocse.perfume.signature;

import com.bocse.perfume.data.TPCNoteType;
import com.bocse.perfume.utils.TextUtils;

import java.util.*;

/**
 * Created by bocse on 27.12.2015.
 */
@Deprecated
public class TPCSignatureEvaluator {

    private static String[] animalic = new String[]
            {"ambergris",
                    "ambroxide",
                    "ambroxan",
                    "ambrox",
                    "ambrofix",
                    "animalic",
                    "beeswax",
                    "castoreum",
                    "civet",
                    "hyrax",
                    "hyraceum",
                    "indolic",
                    "musk",
                    "white musk",
                    "galaxolide",
                    "helvetolide"
            };
    private static String[] aquatic = new String[]{"aquatic", "marine", "ozone", "ozonic",
            "calone", "oceanic", "dew"};
    private static String[] aldehydes = new String[]{"aldehydes", "powdery", "powder"};
    private static String[] aromaticHerbal = new String[]{"aromatic", "herbal", "absinthe", "wormwood",
            "anise", "star anise",
            "aromatic", "herbaceous",
            "artemisia",
            "basil",
            "bay leaves",
            "calamus",
            "cannabis", "hemp seed",
            "hemp resin",
            "celery",
            "cucumber",
            "davana",
            "dill",
            "eucalyptus",
            "fennel",
            "henna",
            "lavender", "fougere",
            "mint", "peppermint",
            "myrtle",
            "oregano",
            "parsley", "lovage",
            "rosemary",
            "sage",
            "shiso",
            "tarragon",
            "thyme",
            "watercress",
            "yerba mate"};
    private static String[] alcohol = new String[]{"liqueur", "whisky", "whiskey", "bourbon", "rum", "wine", "cointreau"};
    private static String[] citrus = new String[]{
            "bergamot",
            "bigarade",
            "bitter orange",
            "blood orange",
            "chinotto",
            "myrtle leaf orange",
            "citronella",
            "hedione",
            "citrus",
            "citruses",
            "clementine",
            "grapefruit",
            "kumquat",
            "lemon",
            "verbena",
            "lime",
            "mandarin",
            "neroli",
            "orange",
            "petitgrain",
            "pomelo",
            "quince",
            "tangerine",
            "yuzu"
    };
    private static String[] earthy = new String[]{"earth", "earthy"};
    private static String[] floral = new String[]{
            "floral",
            "amaryllis",
            "hedione",
            "flower",
            "absinth flower",
            "agava flower",
            "amarillys",
            "ambrosia",
            "amyris",
            "apple blossom",
            "apricot blossom",
            "aquatic florals",
            "florals",
            "lotus",
            "lilies",
            "bella donna",
            "black locust",
            "blended floral",
            "broom flower",
            "cacao flower",
            "calycanthus",
            "camellia flower",
            "camomile",
            "cananga & ylang ylang",
            "carnation",
            "cassie/ acacia farnesiana",
            "cereus - night blooming",
            "champaca",
            "cherry blossom",
            "chrysanthemum",
            "corn flower",
            "cotton flower",
            "cyclamen",
            "daffodil",
            "jonquil",
            "dahlia",
            "daisy",
            "senecia",
            "dandelion",
            "datura flower",
            "elderflower",
            "floral chypre",
            "frangipane",
            "frangipani",
            "freesia",
            "fruity floral",
            "gardenia",
            "geranium",
            "gladiolus",
            "grapefruit blossom",
            "green floral",
            "hawthorn",
            "heather",
            "heliotrope",
            "hibiscus flower",
            "honey blossom",
            "honeysuckle",
            "hyacinth",
            "immortelle flower",
            "sables",
            "iris",
            "jasmine",
            "jasmin",
            "jatamansi",
            "karo karounde flower",
            "lemon blossom",
            "lime blossom",
            "linden blossom",
            "lilac",
            "lily",
            "lily of the valley",
            "lotus",
            "magnolia",
            "marigold",
            "mimosa",
            "narcissus",
            "neroli blossom",
            "nicotiana",
            "tobacco flower",
            "oleander",
            "orange blossom",
            "orchid",
            "osmanthus",
            "papaya flower",
            "passion flower",
            "patchouli flower",
            "peach blossom",
            "pear blossom",
            "peony",
            "pikake",
            "hawaiian jasmine",
            "plum blossom",
            "plumeria",
            "tiare",
            "poppy flower",
            "rose",
            "sampaquita",
            "spicebush",
            "sunflower",
            "sweet pea",
            "syringa",
            "tagetes",
            "tamarind blossom",
            "tangerine blossom",
            "tea blossom",
            "tiare",
            "tobacco flower",
            "tuberose",
            "tubereuse",
            "violet",
            "violette",
            "white floral blended",
            "wild flowers",
            "wisteria",
            "ylang ylang",
            "ylangylang"
    };
    private static String[] fruity = new String[]{
            "fruit",
            "fruity",
            "acai berry",
            "apple",
            "apricot",
            "banana",
            "black currant",
            "cassis",
            "blackberry",
            "blend of fruity",
            "blood orange",
            "blueberry",
            "champagne",
            "cherry",
            "coconut",
            "cranberry",
            "dates",
            "dried fruits",
            "fig",
            "goji berry",
            "wolfberry",
            "gooseberry",
            "grape",
            "guava",
            "honeydew",
            "melon",
            "juniper",
            "berry",
            "kiwi",
            "lychee",
            "mango",
            "olive",
            "papaya",
            "passion fruit",
            "peach",
            "nectarine",
            "pear",
            "pimento berry",
            "pineapple",
            "plum",
            "prune",
            "pomegranate",
            "raspberry",
            "red berry",
            "redcurrant",
            "sopadillo",
            "strawberry",
            "watermelon"
    };
    private static String[] sweet = new String[]{
            "vanille",
            "sweets",
            "burnt sugar",
            "almond",
            "amaretto",
            "bubble gum",
            "calisson",
            "caramel",
            "chai",
            "chocolate",
            "cocoa",
            "coconut",
            "coffee",
            "cola",
            "cotton candy",
            "candy",
            "custard",
            "gingerbread",
            "gourmand blended",
            "heliotrope",
            "cherry",
            "honey",
            "kulfi",
            //"licorice",
            "macarons",
            "maple",
            "marshmallow",
            "meringue",
            "mexican chocolate",
            "milk",
            "cream",
            "nougat",
            "nutty",
            "praline",
            "pecan",
            "hazelnut",
            "popcorn",
            "rice",
            "sugar cane",
            "toffee",
            "tonka bean",
            "vanilla",
            "waffle cone",
            "white chocolate"
    };
    private static String[] grassRoot = new String[]{
            "broom",
            "genet",
            "bulrush",
            "calamus",
            "costus root",
            "cypriol",
            "nagamotha",
            "fresh cut grass",
            "ginger root",
            "ginseng",
            "grass",
            "roots",
            "hay",
            "jatamansi",
            "spikenard",
            "lemon grass",
            "orris root",
            "iris",
            "patchouli",
            "snake root",
            "sweet grass",
            "truffle",
            "vetiver",
            "wheat"
    };
    private static String[] green = new String[]{
            "lichen",
            "forest",
            "oakmoss",
            "ink",
            "chypre",
            "agave cactus",
            "black currant buds",
            "carrot leaves",
            "cinnamon leaves",
            "fig leaves",
            "galbanum",
            "grape leaves",
            "green blended",
            "henna",
            "ivy leaves",
            "juniper berries",
            "laurel",
            "litsea cubeba",
            "mint leaves",
            "mint",
            "moss",
            "mossy",
            "petitgrain",
            "pine",
            "evergreens",
            "rhubarb",
            "tomato leaves",
            "violet leaves",
    };
    private static String[] resinIncense = new String[]{
            "camphor",
            "amber",
            "benzoin",
            "styrax",
            "storax",
            "cistus",
            "ladanum",
            "labdanum",
            "rock rose",
            "rose of sharon",
            "copaiba",
            "copahu balm",
            "elemi",
            "frankincense",
            "olibanum",
            "galbanum",
            "incense",
            "mastic",
            "lentisque",
            "myrrh",
            "myrrhe",
            "nag champa",
            "opoponax",
            "peru balsam",
            "resinous",
            "sandaraque",
            "sandarac",
            "tolu balsam",

    };
    private static String[] leather = new String[]{"leather"};
    private static String[] linen = new String[]{"linen", "linnen"};
    private static String[] synthetic = new String[]{"cashmeran"};
    private static String[] naturalistic = new String[]{};
    private static String[] synethetic = new String[]{};
    private static String[] oriental = new String[]{"oriental"};
    private static String[] salty = new String[]{"salty", "salt"};
    private static String[] spice = new String[]{
            "ambrette",
            "angelica",
            "anise",
            "bay leaves",
            "laurel",
            "blended spices",
            "caraway",
            "cardamom",
            "cayenne pepper",
            "chili",
            "cinnamon",
            "clove",
            "coriander",
            "cumin",
            "cumourin",
            "tonka bean",
            "elemi",
            "licorice",
            "mace",
            "massoia bark",
            "nutmeg",
            "paprika",
            "pepper",
            "pimento",
            "pink peppercorn",
            "saffron",
            "sesame",
            "spice",
            "spicy",
            "tamarind",
            "tarragon",
            "tumeric"
    };
    private static String[] tea = new String[]{"tea"};
    private static String[] tobacco = new String[]{"tobacco", "smoke", "smokey", "smoky notes"};
    private static String[] wood = new String[]{
            "african wenge wood",
            "silver fir",
            "agar wood",
            "oud wood",
            "andira wood",
            "atlas cedar",
            "cedar",
            "birch",
            "cedar wood",
            "balsam fir",
            "bamboo wood",
            "beech tree",
            "birch wood",
            "blond wood",
            "camphor wood",
            "cashmere wood",
            "cinchona wood",
            "copaiba balsam",
            "cypress",
            "hinoki",
            "cypriol oil",
            "nagamotha",
            "drift wood",
            "ebony wood",
            "fir tree",
            "gaiac wood",
            "guaiacum",
            "guajaco wood",
            "hemlock",
            "hinoki wood",
            "juniper wood",
            "macassar wood",
            "mahogany",
            "massoia wood",
            "mastic wood",
            "melati wood",
            "oak",
            "palisander",
            "papyrus",
            "pine wood",
            "poplar",
            "redwood",
            "rosewood",
            "sandalwood",
            "sawdust",
            "spruce tree",
            "sycamore",
            "tamboti wood",
            "tanakha wood",
            "teak wood",
            "walnut",
            "willow bark",
            "wood blend",
            "wood",
            "isoesuper"
    };
    private Map<TPCNoteType, Set<String>> allSets;
    private Set<String> animalicSet;
    private Set<String> aquaticSet;
    private Set<String> aldehydesSet;
    private Set<String> aromaticHerbalSet;
    private Set<String> alcoholSet;
    private Set<String> citrusSet;
    private Set<String> earthySet;
    private Set<String> floralSet;
    private Set<String> fruitySet;
    private Set<String> sweetSet;
    private Set<String> grassRootSet;
    private Set<String> greenSet;
    private Set<String> resinIncenseSet;
    private Set<String> leatherSet;
    private Set<String> linenSet;
    private Set<String> naturalisticSet;
    private Set<String> syntheticSet;
    private Set<String> orientalSet;
    /*


Hedione
Helvetolide
Ink
ISO E Super
Javanol
Lorenox
Metallic Notes
Mugane
NOOUD
Orcanox
Paradisone
Rubber
Vinyl
     */
    private Set<String> saltySet;
    private Set<String> spiceSet;
    private Set<String> woodSet;
    private Set<String> teaSet;
    private Set<String> tobaccoSet;

    public TPCSignatureEvaluator() {
        animalicSet = new HashSet<>(Arrays.asList(animalic));
        aquaticSet = new HashSet<>(Arrays.asList(aquatic));
        aldehydesSet = new HashSet<>(Arrays.asList(aldehydes));
        aromaticHerbalSet = new HashSet<>(Arrays.asList(aromaticHerbal));
        alcoholSet = new HashSet<>(Arrays.asList(alcohol));
        citrusSet = new HashSet<>(Arrays.asList(citrus));
        earthySet = new HashSet<>(Arrays.asList(earthy));
        floralSet = new HashSet<>(Arrays.asList(floral));
        fruitySet = new HashSet<>(Arrays.asList(fruity));
        sweetSet = new HashSet<>(Arrays.asList(sweet));
        grassRootSet = new HashSet<>(Arrays.asList(grassRoot));
        greenSet = new HashSet<>(Arrays.asList(green));
        resinIncenseSet = new HashSet<>(Arrays.asList(resinIncense));
        leatherSet = new HashSet<>(Arrays.asList(leather));
        linenSet = new HashSet<>(Arrays.asList(linen));
        naturalisticSet = new HashSet<>(Arrays.asList(naturalistic));
        syntheticSet = new HashSet<>(Arrays.asList(synethetic));
        orientalSet = new HashSet<>(Arrays.asList(oriental));
        saltySet = new HashSet<>(Arrays.asList(salty));
        spiceSet = new HashSet<>(Arrays.asList(spice));
        woodSet = new HashSet<>(Arrays.asList(wood));
        teaSet = new HashSet<>(Arrays.asList(tea));
        tobaccoSet = new HashSet<>(Arrays.asList(tobacco));

        allSets = new LinkedHashMap<>();
        allSets.put(TPCNoteType.ANIMALIC_MUSK, animalicSet);
        allSets.put(TPCNoteType.AQUATIC_MARINE_OZONIC, aquaticSet);
        allSets.put(TPCNoteType.POWDERY_ALDEHYDE, aldehydesSet);
        allSets.put(TPCNoteType.AROMATIC_HERBAL, aromaticHerbalSet);
        allSets.put(TPCNoteType.ALCOHOL, alcoholSet);
        allSets.put(TPCNoteType.CITRUS_HESPERIDE, citrusSet);
        allSets.put(TPCNoteType.EARTHY, earthySet);
        allSets.put(TPCNoteType.FLORAL, floralSet);
        allSets.put(TPCNoteType.FRUITY, fruitySet);
        allSets.put(TPCNoteType.GOURMAND_SWEET, sweetSet);
        allSets.put(TPCNoteType.GRASS_ROOT, grassRootSet);
        allSets.put(TPCNoteType.GREEN, greenSet);
        allSets.put(TPCNoteType.RESIN, resinIncenseSet);
        allSets.put(TPCNoteType.LEATHER, leatherSet);
        allSets.put(TPCNoteType.LINEN, linenSet);
        allSets.put(TPCNoteType.NATURALISTIC_REALISTIC, naturalisticSet);
        allSets.put(TPCNoteType.MODERN_SYNTHETIC, syntheticSet);
        allSets.put(TPCNoteType.ORIENTAL, orientalSet);
        allSets.put(TPCNoteType.SPICY, spiceSet);
        allSets.put(TPCNoteType.WOOD, woodSet);
        allSets.put(TPCNoteType.TEA, teaSet);
        allSets.put(TPCNoteType.TOBACCO, tobaccoSet);

    }

    public TPCNoteType findNoteClassFast(String note) {
        note = note.replaceAll("notes", "").replaceAll("accord", "").trim();
        for (Map.Entry<TPCNoteType, Set<String>> entry : allSets.entrySet()) {
            if (entry.getValue().contains(note))
                return entry.getKey();
        }
        return TPCNoteType.UNKNOWN;
    }

    public TPCNoteType findNoteClassSlow(String note) {
        note = TextUtils.flattenToAscii(note.replaceAll("notes", "").replaceAll("accord", "").trim());
        for (Map.Entry<TPCNoteType, Set<String>> entry : allSets.entrySet()) {
            for (String classNote : entry.getValue()) {
                if (note.contains(classNote))
                    return entry.getKey();
            }

        }
        return TPCNoteType.UNKNOWN;
    }

}
