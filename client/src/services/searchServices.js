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

        return response.data.slice(0, Math.min(20, response.data.length)); 
    } catch (error) {
        console.error("Error fetching search results:", error);
        throw error;
    }
}

export const getSearchHistory = async () => {
    try {
        const response = await axios.get(`${API_URL}/history`);
        
        // Process the data to remove quotes from queries
        // TODO: Change after bonus is implemented (operations on phrase searching)
        const cleanedData = response.data.map(item => {
            const cleanedItem = {...item};
            
            if (typeof cleanedItem.query === 'string') {
                const query = cleanedItem.query;
                if ((query.startsWith('"') && query.endsWith('"')) || 
                    (query.startsWith("'") && query.endsWith("'"))) {
                    cleanedItem.query = query.slice(1, -1);
                }
            }
            
            return cleanedItem;
        });
        
        const uniqueMap = new Map();
        cleanedData.forEach(item => {
            uniqueMap.set(item.query.toLowerCase(), item);
        });
        
        const uniqueData = Array.from(uniqueMap.values());
        
        console.log("Search history response (unique):", uniqueData);
        return uniqueData;
    } catch (error) {
        console.error("Error fetching search history:", error);
        throw error;
    }
}