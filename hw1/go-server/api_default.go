/*
 * Album Store API
 *
 * CS6650 Fall 2023
 *
 * API version: 1.0.0
 * Contact: i.gorton@northeasern.edu
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */
package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
)

func GetAlbumByKey(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	urlString := r.URL.String()

	if urlString == "" {
		http.Error(w, "missing parameters", http.StatusNotFound)
		return
	}
	urlSplit := strings.Split(urlString, "/")
	albumId := urlSplit[3]
	if albumId != "" {
		responseJSON, jsonErr := json.Marshal(albumSample)

		if jsonErr != nil {
			http.Error(w, "Server HTTP 500 Json Error", http.StatusInternalServerError)
			return
		}

		w.WriteHeader(http.StatusOK)
		w.Write(responseJSON)

		//_, writeErr := w.Write(responseJSON)
		//if writeErr != nil {
		//	http.Error(w, "HTTP 500 serverError", http.StatusInternalServerError)
		//	return
		//}
	}
	w.WriteHeader(http.StatusOK)
}

type postResponse struct {
	AlbumId   string `json:"albumID"`
	ImageSize string `json:"imageSize"`
}

func NewAlbum(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	parseError := r.ParseMultipartForm(10 << 20) // parse input up to 10 MB of data
	if parseError != nil {
		http.Error(w, "Can not parse post input multipart form", http.StatusBadRequest)
		return
	}
	_, handler, err := r.FormFile("image")
	if err != nil {
		http.Error(w, "Unable to get image from the request", http.StatusBadRequest)
		return
	}
	// Convert the int64 to String
	sizeAsString := strconv.FormatInt(handler.Size, 10)
	response := postResponse{AlbumId: handler.Filename, ImageSize: sizeAsString}
	responseJSON, _ := json.Marshal(response)
	w.WriteHeader(http.StatusOK)
	_, writeErr := w.Write(responseJSON)
	if writeErr != nil {
		http.Error(w, "Error write the response", http.StatusInternalServerError)
		return
	}
}
