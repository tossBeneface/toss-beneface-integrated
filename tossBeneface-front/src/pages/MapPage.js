import React, { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// 카카오맵 SDK 심사 제한으로 Leaflet + OpenStreetMap + Overpass API로 대체 (키 불필요)
const CATEGORY_FILTERS = {
  FD6: 'node["amenity"~"restaurant|fast_food"]',
  MT1: 'node["shop"~"supermarket|department_store"]',
  PM9: 'node["amenity"="pharmacy"]',
  OL7: 'node["amenity"="fuel"]',
  CE7: 'node["amenity"="cafe"]',
  CS2: 'node["shop"="convenience"]',
};

const CATEGORY_COLORS = {
  FD6: "#f04452",
  MT1: "#f66d24",
  PM9: "#12b28c",
  OL7: "#8b95a1",
  CE7: "#8146f6",
  CS2: "#3182f6",
};

const MIN_SEARCH_ZOOM = 15;

function MapPage() {
  const mapRef = useRef(null);

  useEffect(() => {
    const map = L.map(mapRef.current).setView([37.566826, 126.9786567], 16);

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    let currCategory = "";
    let markers = [];
    let abortController = null;

    function removeMarker() {
      markers.forEach((m) => map.removeLayer(m));
      markers = [];
    }

    async function searchPlaces() {
      if (!currCategory) return;
      if (map.getZoom() < MIN_SEARCH_ZOOM) {
        removeMarker();
        return;
      }

      if (abortController) abortController.abort();
      abortController = new AbortController();

      const b = map.getBounds();
      const bbox = `${b.getSouth()},${b.getWest()},${b.getNorth()},${b.getEast()}`;
      const query = `[out:json][timeout:10];${CATEGORY_FILTERS[currCategory]}(${bbox});out 40;`;

      try {
        const res = await fetch("https://overpass-api.de/api/interpreter", {
          method: "POST",
          body: "data=" + encodeURIComponent(query),
          signal: abortController.signal,
        });
        if (!res.ok) return;
        const data = await res.json();

        removeMarker();
        const color = CATEGORY_COLORS[currCategory];
        data.elements.forEach((el) => {
          if (!el.lat || !el.lon) return;
          const t = el.tags || {};
          const name = t.name || t["name:ko"] || "이름 없음";
          const addr =
            t["addr:full"] ||
            [t["addr:city"], t["addr:district"], t["addr:street"], t["addr:housenumber"]]
              .filter(Boolean)
              .join(" ");
          const lines = [`<strong>${name}</strong>`];
          if (addr) lines.push(`📍 ${addr}`);
          if (t.phone) lines.push(`📞 ${t.phone}`);
          if (t.opening_hours) lines.push(`🕐 ${t.opening_hours}`);
          if (t.brand && t.brand !== name) lines.push(`🏷 ${t.brand}`);
          const marker = L.circleMarker([el.lat, el.lon], {
            radius: 8,
            color: "#ffffff",
            weight: 2,
            fillColor: color,
            fillOpacity: 0.95,
          })
            .addTo(map)
            .bindPopup(lines.join("<br>"));
          markers.push(marker);
        });
      } catch (e) {
        if (e.name !== "AbortError") console.error("Overpass search failed:", e);
      }
    }

    map.on("moveend", searchPlaces);

    function changeCategoryClass(el) {
      const category = document.getElementById("category");
      if (!category) return;
      for (let i = 0; i < category.children.length; i++) {
        category.children[i].className = "";
      }
      if (el) el.className = "on";
    }

    function onCategoryClick(e) {
      const li = e.target.closest("li");
      if (!li) return;
      if (li.className === "on") {
        currCategory = "";
        changeCategoryClass();
        removeMarker();
      } else {
        currCategory = li.id;
        changeCategoryClass(li);
        searchPlaces();
      }
    }

    const categoryEl = document.getElementById("category");
    if (categoryEl) categoryEl.addEventListener("click", onCategoryClick);

    return () => {
      if (categoryEl) categoryEl.removeEventListener("click", onCategoryClick);
      if (abortController) abortController.abort();
      map.remove();
    };
  }, []);

  return (
    <div className="min-h-screen flex flex-col m-0 p-0">
      {/* 지도 컨테이너 - 남은 공간을 모두 채움 */}
      <div id="map" ref={mapRef} className="flex-grow w-full" style={{ minHeight: "70vh" }}></div>

      {/* 하단 카테고리 영역 */}
      <ul id="category" className="flex-none p-4">
        <li id="FD6" data-order="0">
          <span className="category_bg store"></span>
          음식점
        </li>
        <li id="MT1" data-order="1">
          <span className="category_bg mart"></span>
          마트
        </li>
        <li id="PM9" data-order="2">
          <span className="category_bg pharmacy"></span>
          약국
        </li>
        <li id="OL7" data-order="3">
          <span className="category_bg oil"></span>
          주유소
        </li>
        <li id="CE7" data-order="4">
          <span className="category_bg cafe"></span>
          카페
        </li>
        <li id="CS2" data-order="5">
          <span className="category_bg store"></span>
          편의점
        </li>
      </ul>
    </div>
  );
}

export default MapPage;
