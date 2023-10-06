package main

type album struct {
	Artist string  `json:"artist"`
	Title  string  `json:"title"`
	Year   string `json:"year"`
}

// albums slice to seed record album data.
var albums = []album{
	{ Artist: "Sex Pistols", Title: "Never Mind The Bollocks!", Year: "1977" },
}