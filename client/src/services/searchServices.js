import axios from "axios";

const API_URL = "http://localhost:8081/api";

export const search = async (searchQuery, pageNumber) => {
    console.log("Search query:", searchQuery);
    console.log("Page number:", pageNumber);
    try {
        const response = await axios.get(`${API_URL}/search`, {
        params: {
            query: searchQuery,
            page: pageNumber,
        },
        });

        return response.data; 
    } catch (error) {
        console.error("Error fetching search results:", error);
        throw error;
    }
}

export const getSearchHistory = async () => {
    try {
        const response = await axios.get(`${API_URL}/history`);
        
        // Process the data to remove quotes from queries
        const cleanedData = response.data.map(item => {
            let query = item.query;
            
            // Remove quotes if they exist at beginning and end
            if (typeof query === 'string') {
                if ((query.startsWith('"') && query.endsWith('"')) || 
                    (query.startsWith("'") && query.endsWith("'"))) {
                    query = query.slice(1, -1);
                }
            }
            
            return query.toLowerCase();
        });
        
        // Remove duplicates using Set
        const uniqueQueries = [...new Set(cleanedData)];
        
        console.log("Search history response (unique strings):", uniqueQueries);
        return uniqueQueries;
    } catch (error) {
        console.error("Error fetching search history:", error);
        throw error;
    }
}