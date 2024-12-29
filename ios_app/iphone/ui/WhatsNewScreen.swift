import SwiftUI
import shared

private let secondaryFontSize = 15.0

struct WhatsNewScreen: View {
    
    // todo remove
    @EnvironmentObject private var fs: Fs
    
    @State private var vm = WhatsNewVm()
    
    var body: some View {
        
        VMView(vm: vm) { state in
            
            List {
                
                ForEach(state.historyItemsUi, id: \.self) { historyItemUi in
                    
                    VStack {
                        
                        HStack {
                            
                            Text(historyItemUi.dateText)
                                .foregroundColor(.secondary)
                                .font(.system(size: secondaryFontSize))
                            
                            Spacer()
                            
                            Text(historyItemUi.timeAgoText)
                                .foregroundColor(.secondary)
                                .font(.system(size: secondaryFontSize))
                        }
                        
                        Text(historyItemUi.title)
                            .foregroundColor(.primary)
                            .font(.system(size: 17, weight: .bold))
                            .padding(.top, 6)
                            .textAlign(.leading)
                        
                        if let text = historyItemUi.text {
                            Text(text)
                                .foregroundColor(.secondary)
                                .font(.system(size: secondaryFontSize))
                                .padding(.top, 6)
                                .textAlign(.leading)
                        }
                        
                        if let buttonUi = historyItemUi.buttonUi {
                            Text(buttonUi.text)
                                .padding(.top, 6)
                                .foregroundColor(.blue)
                                .textAlign(.leading)
                                .onTapGesture {
                                    if (buttonUi == WhatsNewVm.HistoryItemUiButtonUi.pomodoro) {
                                        fs.ReadmeSheet__open(defaultItem: .pomodoro)
                                    } else {
                                        fatalError()
                                    }
                                }
                        }
                    }
                    .padding(.vertical, 2)
                }
            }
            .listStyle(.plain)
            .navigationTitle(state.headerTitle)
        }
    }
}
