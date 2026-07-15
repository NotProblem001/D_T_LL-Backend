package com.dtll.backend.optimization.dto;

import java.util.List;

/** Matriz cuadrada de distancias (m) y duraciones (s) entre `ids.get(i)` e `ids.get(j)`. */
public record MatrizDistancias(List<String> ids, double[][] distanciasM, double[][] duracionesSeg) {
}
