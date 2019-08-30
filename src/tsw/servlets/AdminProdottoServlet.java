/*
BSD 3-Clause License

Copyright (c) 2019, Mattia De Rosa
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tsw.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tsw.model.Categoria;
import tsw.model.Prodotto;
import tsw.model.ProdottoDAO;
import tsw.model.Utente;

/**
 * @author Mattia De Rosa
 *
 */
@WebServlet("/AdminProdotto")
public class AdminProdottoServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private final ProdottoDAO prodottoDAO = new ProdottoDAO();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Utente utente = (Utente) request.getSession().getAttribute("utente");
		if (utente == null || !utente.isAdmin()) {
			throw new MyServletException("Utente non autorizzato");
		}

		String idstr = request.getParameter("id");
		if (idstr != null) {
			if (request.getParameter("rimuovi") != null) {
				prodottoDAO.doDelete(Integer.parseInt(idstr));
				request.setAttribute("notifica", "Prodotto rimosso con successo.");
			} else {
				Prodotto prodotto;
				String nome = request.getParameter("nome");
				String descrizione = request.getParameter("descrizione");
				String prezzoCent = request.getParameter("prezzoCent");
				if (nome != null && descrizione != null && prezzoCent != null) {
					// modifica/aggiunta prodotto
					prodotto = new Prodotto();
					prodotto.setNome(nome);
					prodotto.setDescrizione(descrizione);
					prodotto.setPrezzoCent(Long.parseLong(prezzoCent));

					String[] categorie = request.getParameterValues("categorie");
					prodotto.setCategorie(categorie != null ? Arrays.stream(categorie).map(id -> {
						Categoria c = new Categoria();
						c.setId(Integer.parseInt(id));
						return c;
					}).collect(Collectors.toList()) : Collections.emptyList());

					if (idstr.isEmpty()) { // aggiunta nuovo prodotto
						prodottoDAO.doSave(prodotto);
						request.setAttribute("notifica", "Prodotto aggiunto con successo.");
					} else { // modifica prodotto esistente
						prodotto.setId(Integer.parseInt(idstr));
						prodottoDAO.doUpdate(prodotto);
						request.setAttribute("notifica", "Prodotto modificato con successo.");
					}
				} else {
					int id = Integer.parseInt(idstr);
					prodotto = prodottoDAO.doRetrieveById(id);
				}
				request.setAttribute("prodotto", prodotto);
			}
		}

		RequestDispatcher requestDispatcher = request.getRequestDispatcher("WEB-INF/jsp/adminprodotto.jsp");
		requestDispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
