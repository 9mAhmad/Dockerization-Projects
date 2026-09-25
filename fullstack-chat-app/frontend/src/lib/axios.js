import axios from "axios";
import dotenv from "dotenv";

dotenv.config();



export const axiosInstance = axios.create({
  baseURL: import.meta.env.MODE === "development" ? process.env.VITE_API_URL + "/api" : "/api",
  withCredentials: true,
});


