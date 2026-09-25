import axios from "axios";
import dotenv from "dotenv";

dotenv.config();



export const axiosInstance = axios.create({
  baseURL: import.meta.env.MODE === "development" ? "http://localhost:8088/api" : "/api",
  withCredentials: true,
});


