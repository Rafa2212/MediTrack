package com.example.myapplication;

/**
 * Model class to represent an ICD-10 search result
 */
public class ICDSearchResult {
    private String code;
    private String name;

    /**
     * Constructs a new ICD-10 search result with the specified code and name.
     *
     * @param code The ICD-10 code (e.g., "A00.0")
     * @param name The name or description of the disease associated with the code
     */
    public ICDSearchResult(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Gets the ICD-10 code of this search result.
     *
     * @return The ICD-10 code (e.g., "A00.0")
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the name or description of the disease associated with this ICD-10 code.
     *
     * @return The disease name or description
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of this ICD-10 search result.
     * The string consists of the ICD-10 code followed by a hyphen and the disease name.
     *
     * @return A string in the format "code - name"
     */
    @Override
    public String toString() {
        return code + " - " + name;
    }
}
