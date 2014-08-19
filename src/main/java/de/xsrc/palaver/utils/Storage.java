package de.xsrc.palaver.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datafx.crud.CrudException;
import org.datafx.crud.CrudService;
import org.datafx.util.EntityWithId;
import org.datafx.util.QueryParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Storage<S extends EntityWithId<String>, String> implements
		CrudService<S, String> {

	private Class<S> clazz;
	private static final Logger logger = Logger.getLogger(Storage.class
			.getName());
	private ObservableList<S> cacheList;

	public void delete(S entity) throws CrudException {
		// TODO Auto-generated method stub

	}

	public Storage(Class<S> clazz) {
		super();
		this.clazz = clazz;
	}

	public S save(S entity) throws CrudException {
		getAll().add(entity);
		return null;
	}

	public ObservableList<S> getAll() throws CrudException {
		if (cacheList == null) {
			logger.finer("Initializing cache for model "
					+ this.clazz.getSimpleName());
			cacheList = initialize();

			cacheList.addListener((Change<? extends S> c) -> {
				while (c.next()) {
					if (c.wasPermutated()) {
						logger.finest("Permutated: " + c.getFrom() + " => "
								+ c.getTo());
					} else if (c.wasUpdated()) {
						logger.finest("Updated: " + c.getFrom() + " => "
								+ c.getTo());
					} else {
						if (c.getRemovedSize() > 0)
							logger.finest("Added: " + c.getRemovedSize() + " "
									+ clazz.getSimpleName());
						if (c.getAddedSize() > 0)
							logger.finest("Removed: " + c.getAddedSize() + " "
									+ clazz.getSimpleName());
					}
				}
				saveModel();
			});
		}
		return cacheList;
	}

	private ObservableList<S> initialize() {
		ObservableList<S> result = FXCollections.observableArrayList();
		AppDataSource<S> cs;
		try {
			cs = new AppDataSource<S>(clazz);
			while (cs.next()) {
				S tmp = cs.get();
				result.add(tmp);
			}
			logger.finest("Read " + result.size() + " records from Model "
					+ clazz.getSimpleName());

		} catch (IOException e) {
			logger.warning("Reading data for model " + clazz.getSimpleName()
					+ " failed. First run?");
		}
		return result;
	}

	public S getById(String id) throws CrudException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<S> query(java.lang.String name, QueryParameter... params)
			throws CrudException {
		// TODO Auto-generated method stub
		return null;
	}

	private void saveModel() {
		logger.finest("Trying to save model: " + this.clazz.getSimpleName());
		try {
			DocumentBuilder docBuilder;
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(clazz.getSimpleName()
					.toLowerCase() + "s");
			doc.appendChild(rootElement);

			JAXBContext jc = JAXBContext.newInstance(clazz);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(
					javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			ObservableList<S> list = getAll();
			for (S s : list) {
				marshaller.marshal(s, doc.getFirstChild());
			}

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File file = AppDataSource.getFile(clazz);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			logger.finer("Saved model " + this.clazz.getSimpleName());

		} catch (ParserConfigurationException | JAXBException | CrudException
				| TransformerConfigurationException e) {
			e.printStackTrace();
			logger.severe("This should not happen :-/");
			Platform.exit();
		} catch (TransformerException e) {
			e.printStackTrace();
			logger.severe("Could not transform XML for model "
					+ clazz.getSimpleName() + ". Model data broken?");
			Platform.exit();
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Could not write storage file for  model "
					+ clazz.getSimpleName()
					+ " to disk. Check disk space and access rights");

		}

	}
}