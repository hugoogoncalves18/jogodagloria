package com.jogogloria.model;

import com.example.Biblioteca.Graphs.AdjListGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.utils.SimpleMap;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Representa a estrutura fisica e lógica do Labirinto
 * Esta classe gere:
 * A estrutura matemática de conexões (usando um {@link AdjListGraph}
 * O armazenamento e acesso rápido aos objetos de jogo
 * A validação de movimentos
 *
 * Mantém listas auxiliares para permitir que a Interface gráfica
 * desenhe no mapa iterando sequencialmente
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Labyrinth {

    // --- Estruturas de Dados ---

    /**
     * O grafo que define a conexão entre as salas
     * Os vértices são os IDs das salas e as arestas representam os corredores
     * É utilizado para calcular o caminho mais curto
     */
    public final AdjListGraph<String> graphStructure;

    /** Mapa para acesso O(1)às salas pelo seu ID */
    private final SimpleMap<String, Room> roomMap;

    /**Mapa para acessp O(1) aos corredores pela sua chave */
    private final SimpleMap<String, Corridor> corridorMap;

    /**Mapa para acesso O(1) às alavancas pelo ID da sala onde se encontram */
    private final SimpleMap<String, Lever> leverMap;

    // Listas Auxiliares (Para iteração sequencial)

    /** Lista linear de todas as salas*/
    private final ArrayUnorderedList<Room> storedRooms;

    /** Lista linear de todos os corredores*/
    private final ArrayUnorderedList<Corridor> storedCorridors;

    /** Lista linear de todas as alavancas*/
    private final ArrayUnorderedList<Lever> storedLevers;

    /** Lista de IDs das salas onde os jogadores começam o jogo*/
    private final ArrayUnorderedList<String> entryPoints;

    /** ID da sala inicial*/
    private String startRoomId;

    /** ID da sala do tesouro*/
    private String endRoomId;

    /**
     * Construtor padrão
     * Inicia todas as estruturas de dados vazias
     */
    public Labyrinth() {
        this.graphStructure = new AdjListGraph<>();

        this.roomMap = new SimpleMap<>();
        this.corridorMap = new SimpleMap<>();
        this.leverMap = new SimpleMap<>();

        this.storedRooms = new ArrayUnorderedList<>();
        this.storedCorridors = new ArrayUnorderedList<>();
        this.storedLevers = new ArrayUnorderedList<>();
        this.entryPoints = new ArrayUnorderedList<>();
    }

    /**
     * Adiciona sala ao labirinto
     * Regista a sala no Mapa, no grafo e na lista
     * @param room O objeto Room a adicionar
     */
    public void addRoom(Room room) {
        if (room == null) return;
        String id = room.getId();
        if (!roomMap.containsKey(id)) {
            roomMap.put(id, room);
            graphStructure.addVertex(id);
            storedRooms.addToRear(room);
        }
    }

    /**
     * Adicionar um corredor entre duas salas
     * Cria conexão bidirecional no grafo e armazena o objeto
     *
     * @param idA ID da primeira sala
     * @param idB ID da segunda sala
     */
    public void addCorridor(String idA, String idB) {
        if (!roomMap.containsKey(idA) || !roomMap.containsKey(idB)) return;
        String canonicalKey = getCanonicalCorridorKey(idA, idB);
        if (corridorMap.containsKey(canonicalKey)) return;

        graphStructure.addEdge(idA, idB);
        Corridor corridor = new Corridor(canonicalKey, idA, idB);
        corridorMap.put(canonicalKey, corridor);
        storedCorridors.addToRear(corridor);
    }

    /**
     * Adiciona uma alavanca a uma sala especfica
     * @param roomId ID da sala onde a alavanca está localizada
     * @param lever O objeto alavanca
     */
    public void addLever(String roomId, Lever lever) {
        leverMap.put(roomId, lever);
        storedLevers.addToRear(lever); // [NOVO] Guardar na lista
    }

    /**
     * Obtém um iterador para todas as alavancas existentes no labirinto
     * Essencial para a IA dos Bots, permitindo iterar sobre as alavancas para encontrar
     * a mais próxima quando o caminho está bloqueado
     * @return Iterador de objetos
     */
    public Iterator<Lever> getLeversIterator() {
        return storedLevers.iterator();
    }

    /**
     * Verifica se é possivel mover de uma sala para a outra
     * Valida duas condições
     * 1.Conectividade fisica - Existe uma aresta no grafo entre as duas salas
     * 2. Estado lógico - O corredor que as une está destrancado
     * @param fromId ID da sala de origem
     * @param toId ID da sala de destino
     * @return {@code true} se o movimento for válido, {@code false} caso contrário
     *
     *
     */
    public boolean isValidMove(String fromId, String toId) {
        boolean isConnected = false;
        try {
            isConnected = graphStructure.isConnected();
        } catch (Exception e) { return false; }

        if (!isConnected) return false;
        Corridor c = getCorridor(fromId, toId);
        return c != null && !c.isLocked();
    }

    /**
     * Altera o estado de bloqueio de um corredor
     * @param idA ID de uma das salas
     * @param idB ID da outra sala
     * @param locked {@code true} para trancar, {@code false} para abrir
     */
    public void setCorridorLocked(String idA, String idB, boolean locked) {
        Corridor c = getCorridor(idA, idB);
        if (c != null) c.setLocked(locked);
    }

    // --- Getters e Iteradores ---

    /** @return Iterador para todas as salas*/
    public Iterator<Room> getRoomsIterator() {
        return storedRooms.iterator();
    }

    /** @return Iterador para todos os corredores*/
    public Iterator<Corridor> getCorridorIterator() {
        return storedCorridors.iterator();
    }

    /**
     * Obtém a alavanca localizada numa sala específica
     * @param roomId ID da sala
     * @return O objeto Lever ou {@code null} se não houver alavanca
     */
    public Lever getLever(String roomId) {
        return leverMap.get(roomId);
    }

    /**
     * Obtém uma sala com base nas suas coordenadas de grelha
     * @param x Coordenada X
     * @param y Coordenada Y
     * @return A sala na posição (x,y) ou {@code null}
     */
    public Room getRoomAt(int x, int y) {
        return roomMap.get(x + "-" + y);
    }

    /**
     * Obtém uma sala pelo deu ID
     * @param id ID da sala
     * @return Objeto Room
     */
    public Room getRoom(String id) {
        return roomMap.get(id);
    }

    /**
     * Obtém o objeto Corredor que liga duas salas
     * @param idA Sala A
     * @param idB Sala B
     * @return Objeto Corridor
     */
    public Corridor getCorridor(String idA, String idB) {
        return corridorMap.get(getCanonicalCorridorKey(idA, idB));
    }

    /**
     * Calcula o caminho mais curto entre duas salas
     * @param startId ID de origem
     * @param targetId ID de destino
     * @return Iterador com a sequência de IDs das salas a precorrer
     */
    public Iterator<String> getShortestPath(String startId, String targetId) {
        try {
            return graphStructure.iteratorShortestPath(startId, targetId);
        } catch (EmptyCollectionException | IllegalArgumentException e) {
            return new ArrayUnorderedList<String>().iterator();
        }
    }

    /**
     * Obtém a lista de vizinhos diretos de uma sala
     * @param roomId ID da sala
     * @return Lista de IDs das salas adjacentes
     */
    public ArrayUnorderedList<String> getNeighbors(String roomId) {
        try {
            return graphStructure.getAdjacencyList(roomId);
        } catch (Exception e) {
            return new ArrayUnorderedList<>();
        }
    }

    /**
     * Retorna um iterador BFS a partir de uma sala
     * @param startId da sala inicial
     * @return Iterador BFS
     * @throws EmptyCollectionException Se o grafo estiver vazio
     */
    public Iterator<String> iteratorBFS(String startId) throws EmptyCollectionException {
        return graphStructure.iteratorBFS(startId);
    }

    /**
     * Getter e setters
     * @param id
     */
    public void setStartRoom(String id) { this.startRoomId = id; }
    public String getStartRoomId() { return startRoomId; }
    public void setTreasureRoom(String id) { this.endRoomId = id; }
    public String getTreasureRoom() { return endRoomId; }
    public void addEntryPoint(String id) { entryPoints.addToRear(id); }
    public ArrayUnorderedList<String> getEntryPoints() { return entryPoints; }

    /**
     * Gera uma chave única para um corredor
     * @param idA
     * @param idB
     * @return
     */
    private String getCanonicalCorridorKey(String idA, String idB) {
        return (idA.compareTo(idB) < 0) ? idA + "-" + idB : idB + "-" + idA;
    }
}