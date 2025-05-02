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