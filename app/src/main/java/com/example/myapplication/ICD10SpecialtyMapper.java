package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class maps ICD-10 codes to medical specialties.
 * It provides methods to check if a given ICD-10 code is valid for a specific specialty.
 */
public class ICD10SpecialtyMapper {

    // Map of specialty names to their corresponding ICD-10 code patterns
    private static final Map<String, Pattern[]> SPECIALTY_PATTERNS = new HashMap<>();

    static {
        // Initialize the mapping of specialties to ICD-10 code patterns

        // Cardiology: Primary I00-I99 (Diseases of the circulatory system) and other related codes
        SPECIALTY_PATTERNS.put("cardiologist", new Pattern[] {
            // Primary circulatory system codes
            Pattern.compile("^I\\d{2}(\\.\\d{1,4})?$"),
            // Additional cardiology-related codes
            Pattern.compile("^A18\\.84$"),
            Pattern.compile("^A41\\.9$"),
            Pattern.compile("^D50\\.9$"),
            Pattern.compile("^D53\\.9$"),
            Pattern.compile("^D64\\.9$"),
            Pattern.compile("^D68\\.(311|312|318|32|4|8|9)$"),
            Pattern.compile("^E03\\.5$"),
            Pattern.compile("^E09\\.(0|1|2|3|4|5|6|8|9).*$"),
            Pattern.compile("^E10.*$"),
            Pattern.compile("^E11.*$"),
            Pattern.compile("^E13.*$"),
            Pattern.compile("^E75\\.6$"),
            Pattern.compile("^E78.*$"),
            Pattern.compile("^E86\\.0$"),
            Pattern.compile("^E87\\.(1|5|6|8)$"),
            Pattern.compile("^G45.*$"),
            Pattern.compile("^G46\\.(3|4)$"),
            Pattern.compile("^G90\\.(01|09)$"),
            Pattern.compile("^G93\\.2$"),
            Pattern.compile("^H35\\.03.*$"),
            Pattern.compile("^H35\\.82$"),
            Pattern.compile("^H40\\.05.*$"),
            Pattern.compile("^H47\\.01.*$"),
            Pattern.compile("^H93\\.01.*$"),
            Pattern.compile("^O10.*$"),
            Pattern.compile("^O11.*$"),
            Pattern.compile("^O12.*$"),
            Pattern.compile("^O13.*$"),
            Pattern.compile("^O14.*$"),
            Pattern.compile("^O15.*$"),
            Pattern.compile("^O16.*$"),
            Pattern.compile("^O24.*$"),
            Pattern.compile("^P00\\.0$"),
            Pattern.compile("^P29\\.(2|30)$"),
            Pattern.compile("^Q21.*$"),
            Pattern.compile("^Q22.*$"),
            Pattern.compile("^Q23\\.3$"),
            Pattern.compile("^Q24\\.6$"),
            Pattern.compile("^Q25\\.(6|7).*$"),
            Pattern.compile("^R00.*$"),
            Pattern.compile("^R07.*$"),
            Pattern.compile("^R57\\.(0|9)$"),
            Pattern.compile("^S25\\.4.*$"),
            Pattern.compile("^T46\\.6.*$"),
            Pattern.compile("^T81\\.11.*$"),
            Pattern.compile("^Z03\\.4$"),
            Pattern.compile("^Z82\\.3$"),
            Pattern.compile("^Z83\\.42$"),
            Pattern.compile("^Z86\\.(711|73)$"),
            Pattern.compile("^Z95\\.(1|5)$"),
            Pattern.compile("^Z98\\.61$")
        });

        // Pulmonology: Primary J00-J99 (Diseases of the respiratory system) and other related codes
        SPECIALTY_PATTERNS.put("pneumologist", new Pattern[] {
            // Primary respiratory system codes
            Pattern.compile("^J\\d{2}(\\.\\d{1,4})?$"),
            // Additional pulmonology-related codes
            Pattern.compile("^A0103$"),
            Pattern.compile("^A0222$"),
            Pattern.compile("^A065$"),
            Pattern.compile("^A15.*$"),
            Pattern.compile("^A16.*$"),
            Pattern.compile("^A37.*$"),
            Pattern.compile("^A403$"),
            Pattern.compile("^A5004$"),
            Pattern.compile("^A5272$"),
            Pattern.compile("^A5484$"),
            Pattern.compile("^B012$"),
            Pattern.compile("^B052$"),
            Pattern.compile("^B0681$"),
            Pattern.compile("^B206$"),
            Pattern.compile("^B671$"),
            Pattern.compile("^B7781$"),
            Pattern.compile("^B953$"),
            Pattern.compile("^B96(0|1)$"),
            Pattern.compile("^C33$"),
            Pattern.compile("^C34.*$"),
            Pattern.compile("^C39(0|9)$"),
            Pattern.compile("^C465.*$"),
            Pattern.compile("^C780.*$"),
            Pattern.compile("^C7A090$"),
            Pattern.compile("^D021$"),
            Pattern.compile("^D022.*$"),
            Pattern.compile("^D142$"),
            Pattern.compile("^D143.*$"),
            Pattern.compile("^D381$"),
            Pattern.compile("^D3A090$"),
            Pattern.compile("^D86(0|2)$"),
            Pattern.compile("^F18.*$"),
            Pattern.compile("^G001$"),
            Pattern.compile("^G4732$"),
            Pattern.compile("^I2723$"),
            Pattern.compile("^J09.*$"),
            Pattern.compile("^J10.*$"),
            Pattern.compile("^J11.*$"),
            Pattern.compile("^J12.*$"),
            Pattern.compile("^J13$"),
            Pattern.compile("^J14$"),
            Pattern.compile("^J15.*$"),
            Pattern.compile("^J16.*$"),
            Pattern.compile("^J17.*$"),
            Pattern.compile("^J18.*$"),
            Pattern.compile("^J20.*$"),
            Pattern.compile("^J3.*$"),
            Pattern.compile("^J4.*$"),
            Pattern.compile("^J5.*$"),
            Pattern.compile("^J6.*$"),
            Pattern.compile("^J7.*$"),
            Pattern.compile("^J8.*$"),
            Pattern.compile("^J9.*$"),
            Pattern.compile("^M051.*$"),
            Pattern.compile("^M301$"),
            Pattern.compile("^M3213$"),
            Pattern.compile("^M3481$"),
            Pattern.compile("^M3502$"),
            Pattern.compile("^N80B2$"),
            Pattern.compile("^O2902.*$"),
            Pattern.compile("^P23.*$"),
            Pattern.compile("^Q32.*$"),
            Pattern.compile("^Q33.*$"),
            Pattern.compile("^R06.*$"),
            Pattern.compile("^R071$"),
            Pattern.compile("^R91.*$"),
            Pattern.compile("^S110.*$"),
            Pattern.compile("^S170.*$"),
            Pattern.compile("^S1982.*$"),
            Pattern.compile("^S27(3|4|5).*$"),
            Pattern.compile("^T17.*$"),
            Pattern.compile("^T18(10|11|12|19).*$"),
            Pattern.compile("^T27.*$"),
            Pattern.compile("^T410.*$"),
            Pattern.compile("^T71.*$"),
            Pattern.compile("^T8181.*$"),
            Pattern.compile("^T863.*$"),
            Pattern.compile("^T8681.*$"),
            Pattern.compile("^W7.*$"),
            Pattern.compile("^W8.*$"),
            Pattern.compile("^W930.*$"),
            Pattern.compile("^W931.*$"),
            Pattern.compile("^X13.*$"),
            Pattern.compile("^X14.*$"),
            Pattern.compile("^Y364(6|7).*$"),
            Pattern.compile("^Y374(6|7).*$"),
            Pattern.compile("^Z03822$"),
            Pattern.compile("^Z2911$"),
            Pattern.compile("^Z3684$"),
            Pattern.compile("^Z4824$"),
            Pattern.compile("^Z48280$"),
            Pattern.compile("^Z7951$"),
            Pattern.compile("^Z801$"),
            Pattern.compile("^Z825$"),
            Pattern.compile("^Z851.*$"),
            Pattern.compile("^Z8701$"),
            Pattern.compile("^Z902$"),
            Pattern.compile("^Z94(2|3)$"),
            Pattern.compile("^Z983$"),
            Pattern.compile("^Z991.*$")
        });

        // Neurology: G00-G99 (Diseases of the nervous system)
        SPECIALTY_PATTERNS.put("neurology", new Pattern[] {
            Pattern.compile("^G\\d{2}(\\.\\d{1,4})?$")
        });

        // Gastroenterology: K00-K93 (Diseases of the digestive system)
        SPECIALTY_PATTERNS.put("gastroenterology", new Pattern[] {
            Pattern.compile("^K\\d{2}(\\.\\d{1,4})?$")
        });

        // Dermatology: L00-L99 (Diseases of the skin and subcutaneous tissue)
        SPECIALTY_PATTERNS.put("dermatology", new Pattern[] {
            Pattern.compile("^L\\d{2}(\\.\\d{1,4})?$")
        });

        // Endocrinology: E00-E89 (Endocrine, nutritional and metabolic diseases)
        SPECIALTY_PATTERNS.put("endocrinology", new Pattern[] {
            Pattern.compile("^E\\d{2}(\\.\\d{1,4})?$")
        });

        // Nephrology: N00-N99 (Diseases of the genitourinary system)
        SPECIALTY_PATTERNS.put("nephrology", new Pattern[] {
            Pattern.compile("^N\\d{2}(\\.\\d{1,4})?$")
        });

        // Orthopedics: M00-M99 (Diseases of the musculoskeletal system and connective tissue)
        SPECIALTY_PATTERNS.put("orthopedics", new Pattern[] {
            Pattern.compile("^M\\d{2}(\\.\\d{1,4})?$")
        });

        // Hematology: D50-D89 (Diseases of the blood and blood-forming organs)
        SPECIALTY_PATTERNS.put("hematology", new Pattern[] {
            Pattern.compile("^D[5-8]\\d(\\.\\d{1,4})?$")
        });

        // Infectious Disease: A00-B99 (Certain infectious and parasitic diseases)
        SPECIALTY_PATTERNS.put("infectious disease", new Pattern[] {
            Pattern.compile("^[AB]\\d{2}(\\.\\d{1,4})?$")
        });
    }

    /**
     * Checks if the given ICD-10 code is valid for the specified specialty.
     * 
     * @param icd10Code The ICD-10 code to check
     * @param specialty The medical specialty to check against
     * @return true if the code is valid for the specialty, false otherwise
     */
    public static boolean isCodeValidForSpecialty(String icd10Code, String specialty) {
        // If specialty is empty or null, return false
        if (specialty == null || specialty.isEmpty()) {
            return false;
        }

        // Convert specialty to lowercase for case-insensitive matching
        String specialtyLower = specialty.toLowerCase();

        // Get the patterns for the specialty
        Pattern[] patterns = SPECIALTY_PATTERNS.get(specialtyLower);

        // If no patterns are defined for this specialty, return false
        if (patterns == null) {
            return false;
        }

        // Check if the ICD-10 code matches any of the patterns for this specialty
        for (Pattern pattern : patterns) {
            if (pattern.matcher(icd10Code).matches()) {
                return true;
            }
        }

        // If no patterns matched, the code is not valid for this specialty
        return false;
    }

    /**
     * Gets a description of valid ICD-10 codes for the specified specialty.
     * 
     * @param specialty The medical specialty
     * @return A description of valid ICD-10 codes for the specialty
     */
    public static String getValidCodesDescription(String specialty) {
        if (specialty == null || specialty.isEmpty()) {
            return "No specialty specified";
        }

        String specialtyLower = specialty.toLowerCase();

        switch (specialtyLower) {
            case "cardiologist":
                return "Primary: I00-I99 (Diseases of the circulatory system)\n" +
                       "Additional codes include: A18.84 (Tuberculosis of heart), A41.9 (Sepsis), " +
                       "D50.9-D68.9 (Blood disorders), E03.5-E87.8 (Endocrine disorders), " +
                       "G45-G93.2 (Neurological conditions affecting the heart), " +
                       "H35.03-H93.01 (Eye and ear conditions related to cardiovascular issues), " +
                       "O10-O24 (Pregnancy-related hypertension and diabetes), " +
                       "P00.0-P29.30 (Neonatal cardiovascular conditions), " +
                       "Q21-Q25.7 (Congenital heart defects), R00-R57.9 (Cardiovascular symptoms), " +
                       "S25.4-T81.11 (Injuries and complications), Z03.4-Z98.61 (Cardiovascular history and status)";
            case "pneumologist":
                return "Primary: J00-J99 (Diseases of the respiratory system)\n" +
                       "Additional codes include: A0103-A5484 (Infectious respiratory conditions), " +
                       "B012-B96.1 (Viral and bacterial respiratory infections), " +
                       "C33-C7A090 (Respiratory tract neoplasms), D021-D3A090 (Respiratory tract tumors), " +
                       "F18 (Inhalant-related disorders), G001-G4732 (Neurological conditions affecting respiration), " +
                       "I2723 (Pulmonary hypertension), M051-M3502 (Rheumatological conditions with lung involvement), " +
                       "N80B2 (Endometriosis of lung), O2902 (Pregnancy-related respiratory conditions), " +
                       "P23-Q33 (Congenital and neonatal respiratory conditions), " +
                       "R06-R91 (Respiratory symptoms and findings), S110-T8681 (Respiratory injuries and complications), " +
                       "W7-Z991 (External causes and history related to respiratory conditions)";
            case "neurology":
                return "G00-G99: Diseases of the nervous system (e.g., G40.9 for Epilepsy)";
            case "gastroenterology":
                return "K00-K93: Diseases of the digestive system (e.g., K29.7 for Gastritis)";
            case "dermatology":
                return "L00-L99: Diseases of the skin and subcutaneous tissue (e.g., L40.0 for Psoriasis)";
            case "endocrinology":
                return "E00-E89: Endocrine, nutritional and metabolic diseases (e.g., E11.9 for Type 2 diabetes)";
            case "nephrology":
                return "N00-N99: Diseases of the genitourinary system (e.g., N18.9 for Chronic kidney disease)";
            case "orthopedics":
                return "M00-M99: Diseases of the musculoskeletal system and connective tissue (e.g., M54.5 for Low back pain)";
            case "hematology":
                return "D50-D89: Diseases of the blood and blood-forming organs (e.g., D50.9 for Iron deficiency anemia)";
            case "infectious disease":
                return "A00-B99: Certain infectious and parasitic diseases (e.g., A09.9 for Infectious gastroenteritis)";
            default:
                return "Unknown specialty: " + specialty;
        }
    }
}
